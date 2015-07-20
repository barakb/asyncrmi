package org.async.rmi.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.async.rmi.*;
import org.async.rmi.messages.CancelInvokeRequest;
import org.async.rmi.messages.InvokeRequest;
import org.async.rmi.messages.Response;
import org.async.rmi.modules.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.rmi.Remote;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by Barak Bar Orion
 * 28/10/14.
 */
public class ObjectRef {
    private static final Logger logger = LoggerFactory.getLogger(ObjectRef.class);

    private final Remote impl;
    private final Map<Long, OneWay> oneWayMap;
    private final String implClassName;
    private Map<Long, Method> methodIdToMethodMap;
    private final Set<Long> resultSetSet;
    private Map<Long, Trace> traceMap;
    private Map<String, CompletableFuture> inProgressCalls;
    public static final AttributeKey<ServerResultSetCallback> SERVER_RESULT_SET_CALLBACK_ATTRIBUTE_KEY
            =  AttributeKey.valueOf("serverResultSetCallback");

    public ObjectRef(Remote impl, Class[] remoteInterfaces, Map<Long, OneWay> oneWayMap, Set<Long> resultSetSet,
                     Map<Long, Trace> traceMap, String implClassName) {
        this.impl = impl;
        this.oneWayMap = oneWayMap;
        this.resultSetSet = resultSetSet;
        this.traceMap = traceMap;
        this.methodIdToMethodMap = createMethodIdToMethodMap(remoteInterfaces);
        this.implClassName = implClassName;
        this.inProgressCalls = new ConcurrentHashMap<>();
    }

    public void invoke(InvokeRequest invokeRequest, ChannelHandlerContext ctx) {
        Method method = methodIdToMethodMap.get(invokeRequest.getMethodId());
        boolean isResultSet = resultSetSet.contains(invokeRequest.getMethodId());
        OneWay oneWay = oneWayMap.get(invokeRequest.getMethodId());
        if (method == null) {
            logger.error("Unknown method id {} in request {} of object ", invokeRequest.getMethodId(), invokeRequest, impl);
            if (oneWay == null) {
                writeResponse(ctx, new Response(invokeRequest.getRequestId()
                        , null, invokeRequest.callDescription(),
                        new IllegalArgumentException("Unknown method id " + invokeRequest.getMethodId() + " in request " + invokeRequest + " of object " + impl))
                        , invokeRequest);
            }
            return;
        }
        invokeRequest.setMethodName(method.getName());
        invokeRequest.setImplClassName(implClassName);
        trace(invokeRequest, ctx);
        try {
            Object[] params = Modules.getInstance().getUtil().unMarshalParams(invokeRequest.getParams());
            Object res;
            if(isResultSet){
                invokeInThread(method, impl, params, ctx);
                return;
            }else{
                res = method.invoke(impl, params);
            }
            if (oneWay != null) {
                return;
            }
            if (res instanceof Future) {
                final CompletableFuture<Object> completableFuture = toCompletableFuture((Future) res);
                inProgressCalls.put(invokeRequest.getUniqueId(), completableFuture);
                //noinspection unchecked
                completableFuture.whenComplete((o, e) -> {
                    if(null == inProgressCalls.remove(invokeRequest.getUniqueId())){
                       // future was canceled by client.
                       return;
                    }
                    if (o != null) {
                        writeResponse(ctx, new Response(invokeRequest.getRequestId(), o, invokeRequest.callDescription()), invokeRequest);
                    } else {
                        writeResponse(ctx, new Response(invokeRequest.getRequestId(), null, invokeRequest.callDescription(), e), invokeRequest);
                    }
                });
            } else {
                writeResponse(ctx, new Response(invokeRequest.getRequestId(), res, invokeRequest.callDescription()), invokeRequest);
            }
        } catch (IllegalAccessException e) {
            logger.error("error while processing request {} object is {} method is {}", invokeRequest, impl, method.toGenericString(), e);
            writeResponse(ctx, new Response(invokeRequest.getRequestId(), null, invokeRequest.callDescription(), e), invokeRequest);
        } catch (InvocationTargetException e) {
            logger.error("error while processing request {} object is {} method is {}", invokeRequest, impl, method.toGenericString(), e);
            writeResponse(ctx, new Response(invokeRequest.getRequestId(), null, invokeRequest.callDescription(), e.getTargetException()), invokeRequest);
        } catch (Throwable e) {
            logger.error("error while processing request {} object is {} method is {}", invokeRequest, impl, method.toGenericString(), e);
            writeResponse(ctx, new Response(invokeRequest.getRequestId(), null, invokeRequest.callDescription(), e), invokeRequest);
        }
    }

    private Object invokeInThread(final Method method, final Remote impl, final Object[] params, final ChannelHandlerContext ctx) {
        new Thread(() -> {
            try {
                ServerResultSetCallback<Object> serverResultSetCallback = new ServerResultSetCallback<>(ctx);
                ResultSets.set(serverResultSetCallback);
                ctx.attr(SERVER_RESULT_SET_CALLBACK_ATTRIBUTE_KEY).set(serverResultSetCallback);
                method.invoke(impl, params);
            }catch(Exception ignored){
            }
        }).start();
        return null;
    }


    public void cancelRequest(CancelInvokeRequest request) {
        CompletableFuture future = inProgressCalls.remove(request.getUniqueId());
        if(future != null) {
            future.cancel(request.isMayInterruptIfRunning());
        }
    }

    private void trace(InvokeRequest invokeRequest, ChannelHandlerContext ctx) {
        Trace trace = traceMap.get(invokeRequest.getMethodId());
        if (trace != null && trace.value() != TraceType.OFF) {
            if(trace.value() == TraceType.DETAILED) {
                logger.debug("{} <-- {} : {}", getTo(ctx), getFrom(ctx), invokeRequest.toDetailedString());
            }else {
                logger.debug("{} <-- {} : {}", getTo(ctx), getFrom(ctx), invokeRequest);
            }
        }
    }

    private void trace(ChannelHandlerContext ctx, Response response, long methodId) {
        Trace trace = traceMap.get(methodId);
        if (trace != null && trace.value() != TraceType.OFF) {
            logger.debug("{} --> {} : {}", getFrom(ctx), getTo(ctx), response);
        }
    }

    private CompletableFuture<Object> toCompletableFuture(Future future) {
        if (future instanceof CompletableFuture) {
            //noinspection unchecked
            return (CompletableFuture<Object>) future;
        } else {
            CompletableFuture<Object> res = new CompletableFuture<>();
            CompletableFuture.runAsync(() -> {
                try {
                    //noinspection unchecked
                    res.complete(future.get());
                } catch (InterruptedException e) {
                    res.completeExceptionally(e);
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    res.completeExceptionally(e.getCause());
                }
            });
            return res;
        }
    }


    private void writeResponse(ChannelHandlerContext ctx, Response response, InvokeRequest invokeRequest) {
        trace(ctx, response, invokeRequest.getMethodId());
        ctx.writeAndFlush(response);
    }

    private String getFrom(ChannelHandlerContext ctx) {
        return addressAsString((InetSocketAddress) ctx.channel().localAddress());
    }

    private String addressAsString(InetSocketAddress socketAddress) {
        return socketAddress.getHostString() + ":" + socketAddress.getPort();
    }

    private String getTo(ChannelHandlerContext ctx) {
        return addressAsString((InetSocketAddress) ctx.channel().remoteAddress());
    }


    private Map<Long, Method> createMethodIdToMethodMap(Class[] remoteInterfaces) {
        Util util = Modules.getInstance().getUtil();
        List<Method> sortedMethodList = util.getSortedMethodList(remoteInterfaces);
        Map<Long, Method> mapping = new HashMap<>(sortedMethodList.size());
        for (Method method : sortedMethodList) {
            mapping.put(util.computeMethodHash(method), method);
        }
        return mapping;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObjectRef objectRef = (ObjectRef) o;

        return impl.equals(objectRef.impl);

    }

    @Override
    public int hashCode() {
        return impl.hashCode();
    }
}
