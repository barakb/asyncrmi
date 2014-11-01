package org.async.rmi.client;

import org.async.rmi.Connection;
import org.async.rmi.Modules;
import org.async.rmi.messages.Message;
import org.async.rmi.messages.Request;
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
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
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

    public UnicastRef() {
    }

    public UnicastRef(RemoteObjectAddress remoteObjectAddress, Class[] remoteInterfaces, long objectid) {
        this.remoteObjectAddress = remoteObjectAddress;
        this.remoteInterfaces = remoteInterfaces;
        this.objectid = objectid;
    }

    public long getObjectid() {
        return objectid;
    }

    @Override
    public Object invoke(Remote obj, Method method, Object[] params, long opHash) throws Throwable {
        Request request = new Request(nextRequestId.getAndIncrement(), remoteObjectAddress.getObjectId(), opHash, params, method.getName());
        CompletableFuture<Response> future = send(request);
        if(CompletableFuture.class.equals(method.getReturnType())){
//            return future.thenApply(Response::getResult);
            //noinspection unchecked
            CompletableFuture<Object> result = new CompletableFuture();
            future.handle((response, throwable) -> {
                if(null != throwable){
                    result.completeExceptionally(throwable);
                }else if(response.isError()){
                    result.completeExceptionally(response.getError());
                }else{
                    result.complete(response.getResult());
                }
                return null;
            });
            return result;
        }else {
            return getResponseResult(future.get());
        }
    }

    private CompletableFuture<Response> send(Request request) {
        logger.debug("--> {}", request);
        final CompletableFuture<Response> responseFuture = new CompletableFuture<>();
        Modules.getInstance().getTransport().addResponseFuture(request.getRequestId(), responseFuture);
        CompletableFuture<Connection<Message>> connectionFuture = pool.get();
        connectionFuture.whenComplete((connection, throwable) -> {
            if (throwable != null) {
                responseFuture.completeExceptionally(throwable);
            } else {
                connection.send(request, responseFuture);
            }
        });
        return responseFuture;
    }

    private Object getResponseResult(Response response) throws Throwable {
        if(response.isError()){
            throw response.getError();
        }else{
            return response.getResult();
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(remoteObjectAddress);
        out.writeObject(remoteInterfaces);
        out.writeLong(objectid);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        remoteObjectAddress = (RemoteObjectAddress) in.readObject();
        remoteInterfaces = (Class[]) in.readObject();
        objectid = in.readLong();
        pool = createPool();
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
        NettyClientConnectionFactory factory = new NettyClientConnectionFactory(Modules.getInstance().getTransport().getClientEventLoopGroup(), remoteObjectAddress);
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
