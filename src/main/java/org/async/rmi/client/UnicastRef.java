package org.async.rmi.client;

import org.async.rmi.*;
import org.async.rmi.messages.CancelInvokeRequest;
import org.async.rmi.messages.Message;
import org.async.rmi.messages.InvokeRequest;
import org.async.rmi.messages.Response;
import org.async.rmi.netty.NettyClientConnectionFactory;
import org.async.rmi.pool.Pool;
import org.async.rmi.pool.ShrinkableConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Barak Bar Orion
 * 27/10/14.
 */
@SuppressWarnings({"UnusedDeclaration", "SpellCheckingInspection"})
public class UnicastRef implements RemoteRef {
    private static final Logger logger = LoggerFactory.getLogger(UnicastRef.class);
    private RemoteObjectAddress remoteObjectAddress;
    private Class[] remoteInterfaces;
    private static final AtomicLong nextRequestId = new AtomicLong(0);
    private Pool<Connection<Message>> pool;
    private long objectid;
    private Map<Long, Trace> traceMap;
    private String callDescription;
    private UUID clientId;
    private RequestQueue requestQueue;

    public UnicastRef() {
        clientId = UUID.randomUUID();
        requestQueue = new RequestQueue(this);
    }

    public UnicastRef(RemoteObjectAddress remoteObjectAddress, Class[] remoteInterfaces, long objectid
            , Map<Long, Trace> traceMap, String callDescription) {
        this.remoteObjectAddress = remoteObjectAddress;
        this.remoteInterfaces = remoteInterfaces;
        this.objectid = objectid;
        this.traceMap = traceMap;
        this.callDescription = callDescription;
    }

    public long getObjectid() {
        return objectid;
    }

    @Override
    public Object invoke(Remote obj, Method method, Object[] params, long opHash, OneWay oneWay, boolean isResultSet) throws Throwable {

        Modules.getInstance().getTransport().startClassLoaderServer(Thread.currentThread().getContextClassLoader());

        MarshalledObject [] marshalledParams = Modules.getInstance().getUtil().marshalParams(params);

        final InvokeRequest invokeRequest = new InvokeRequest(nextRequestId.getAndIncrement()
                , remoteObjectAddress.getObjectId(), opHash, oneWay != null
                , marshalledParams, method.getName(), callDescription);
        final CompletableFuture<Object> result = new ClientCompletableFuture<>(mayInterruptIfRunning -> send(new CancelInvokeRequest(invokeRequest, mayInterruptIfRunning), null, false, null));
        SendResult sendResult = send(invokeRequest, oneWay, isResultSet, result);
        CompletableFuture<Response> future = sendResult != null ? sendResult.responseFuture : null;
        if (future != null && oneWay == null && Future.class.isAssignableFrom(method.getReturnType())) {
            //noinspection unchecked
            future.handle((response, throwable) -> {
                if (null != throwable) {
                    result.completeExceptionally(throwable);
                } else if (response.isError()) {
                    result.completeExceptionally(response.getError());
                } else {
                    result.complete(response.getResult());
                }
                return null;
            });
            return result;
        } else if (oneWay != null) {
            if (future != null && Future.class.isAssignableFrom(method.getReturnType())) {
                //noinspection unchecked
                future.handle((response, throwable) -> {
                    if (null != throwable) {
                        result.completeExceptionally(throwable);
                    } else {
                        result.complete(null);
                    }
                    return null;
                });
                return result;
            } else if (oneWay.full()) {
                return null;
            } else {
                return getResponseResult(translateClientError(future));
            }
        } else if(isResultSet && (sendResult != null)){
            ClientResultSet clientResultSet = new ClientResultSet(sendResult.connectionFuture);
            sendResult.connectionFuture.get().attach(clientResultSet);
            clientResultSet.readyFuture().get();
            return clientResultSet;
        } else {
            return getResponseResult(translateClientError(future));
        }
    }

    @Override
    public void close() throws IOException {
        if(pool != null) {
            pool.close();
        }
    }

    public synchronized void redirect(long objectId, String host, int port) {
        RemoteObjectAddress redirectedAddress = new RemoteObjectAddress("rmi://" + host + ":" + port, objectId);
        logger.info("redirecting client from {} to {}", remoteObjectAddress, redirectedAddress);
        this.objectid = objectId;
        remoteObjectAddress = redirectedAddress;
        Pool<Connection<Message>> oldPool = pool;
        pool = createPool();
        if(oldPool != null) {
            try {
                oldPool.close();
            }catch(Exception e){
                logger.error(e.toString(), e);
            }
        }
    }

    private SendResult send(InvokeRequest invokeRequest, OneWay oneWay, boolean isResultSet, CompletableFuture<Object> result) {
        if(!requestQueue.add(invokeRequest, oneWay, result)){
            return null;
        }
        final CompletableFuture<Response> responseFuture = new CompletableFuture<>();
        if(!isResultSet) {
            Modules.getInstance().getTransport().addResponseFuture(invokeRequest, responseFuture, traceMap.get(invokeRequest.getMethodId()));
        }
        CompletableFuture<Connection<Message>> connectionFuture = pool.get();
        connectionFuture.whenComplete((connection, throwable) -> {
            requestQueue.processRequest(connection, throwable, responseFuture);
            if(!isResultSet) {
                pool.free(connection);
            }
        });
        return new SendResult(responseFuture, connectionFuture);
    }

    class SendResult{
        public SendResult(CompletableFuture<Response> responseFuture, CompletableFuture<Connection<Message>> connectionFuture) {
            this.responseFuture = responseFuture;
            this.connectionFuture = connectionFuture;
        }
        CompletableFuture<Response> responseFuture;
        CompletableFuture<Connection<Message>> connectionFuture;
    }

    void trace(InvokeRequest invokeRequest, Connection<Message> connection) {
        Trace trace = traceMap.get(invokeRequest.getMethodId());
        if(trace != null && trace.value() != TraceType.OFF) {
            if(trace.value() == TraceType.DETAILED){
                logger.debug("{} --> {} : {}", connection.getLocalAddress(), connection.getRemoteAddress(), invokeRequest.toDetailedString());
            }else{
                logger.debug("{} --> {} : {}", connection.getLocalAddress(), connection.getRemoteAddress(), invokeRequest);
            }
        }
    }

    public <T> T translateClientError(Future<T> future) throws Throwable {
        try {
            return future.get();
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                throw e;
            } else if (e instanceof ExecutionException) {
                throw e.getCause();
            } else {
                throw e;
            }
        }
    }

    private Object getResponseResult(Response response) throws Throwable {
        if (response == null) {
            return null;
        }
        if (response.isError()) {
            //noinspection ThrowableResultOfMethodCallIgnored
            Throwable t = response.getError();
            return translateServerError(t);
        } else {
            return response.getResult();
        }
    }

    private Object translateServerError(Throwable t) throws Throwable {
        if (t instanceof RemoteException) {
            throw t;
        } else {
            throw new RemoteException(t.toString() + " from server", t);
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(remoteObjectAddress);
        out.writeObject(remoteInterfaces);
        out.writeObject(traceMap);
        out.writeLong(objectid);
        out.writeUTF(callDescription);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        remoteObjectAddress = (RemoteObjectAddress) in.readObject();
        remoteInterfaces = (Class[]) in.readObject();
        //noinspection unchecked
        traceMap = (Map<Long, Trace>) in.readObject();
        objectid = in.readLong();
        callDescription = in.readUTF();
        pool = createPool();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UnicastRef that = (UnicastRef) o;

        return remoteObjectAddress.equals(that.remoteObjectAddress);

    }

    @Override
    public int hashCode() {
        return remoteObjectAddress.hashCode();
    }

    private Pool<Connection<Message>> createPool() {
        pool = new ShrinkableConnectionPool(2);
        NettyClientConnectionFactory factory = new NettyClientConnectionFactory(Modules.getInstance().getTransport().getClientEventLoopGroup(), remoteObjectAddress, clientId);
        factory.setPool(pool);
        pool.setFactory(factory);
        return pool;
    }


    @Override
    public String toString() {
        return "UnicastRef{" +
                "remoteObjectAddress=" + remoteObjectAddress +
                ", remoteInterfaces=" + Arrays.toString(remoteInterfaces) +
                '}';
    }
}
