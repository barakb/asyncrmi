package org.async.rmi.server;

import io.netty.channel.ChannelHandlerContext;
import org.async.rmi.Modules;
import org.async.rmi.OneWay;
import org.async.rmi.Trace;
import org.async.rmi.TraceType;
import org.async.rmi.messages.CancelRequest;
import org.async.rmi.messages.Request;
import org.async.rmi.messages.Response;
import org.async.rmi.modules.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.rmi.Remote;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private Map<Long, Trace> traceMap;
    private Map<String, CompletableFuture> inProgressCalls;

    public ObjectRef(Remote impl, Class[] remoteInterfaces, Map<Long, OneWay> oneWayMap, Map<Long, Trace> traceMap, String implClassName) {
        this.impl = impl;
        this.oneWayMap = oneWayMap;
        this.traceMap = traceMap;
        this.methodIdToMethodMap = createMethodIdToMethodMap(remoteInterfaces);
        this.implClassName = implClassName;
        this.inProgressCalls = new ConcurrentHashMap<>();
    }

    public void invoke(Request request, ChannelHandlerContext ctx) {
        Method method = methodIdToMethodMap.get(request.getMethodId());
        OneWay oneWay = oneWayMap.get(request.getMethodId());
        if (method == null) {
            logger.error("Unknown method id {} in request {} of object ", request.getMethodId(), request, impl);
            if (oneWay == null) {
                writeResponse(ctx, new Response(request.getRequestId()
                        , null, request.callDescription(),
                        new IllegalArgumentException("Unknown method id " + request.getMethodId() + " in request " + request + " of object " + impl))
                        , request);
            }
            return;
        }
        request.setMethodName(method.getName());
        request.setImplClassName(implClassName);
        trace(request, ctx);
        try {
            Object[] params = Modules.getInstance().getUtil().unMarshalParams(request.getParams());
            final Object res = method.invoke(impl, params);
            if (oneWay != null) {
                return;
            }
            if (res instanceof Future) {
                final CompletableFuture<Object> completableFuture = toCompletableFuture((Future) res);
                inProgressCalls.put(request.getUniqueId(), completableFuture);
                //noinspection unchecked
                completableFuture.whenComplete((o, e) -> {
                    if(null == inProgressCalls.remove(request.getUniqueId())){
                       // future was canceled by client.
                       return;
                    }
                    if (o != null) {
                        writeResponse(ctx, new Response(request.getRequestId(), o, request.callDescription()), request);
                    } else {
                        writeResponse(ctx, new Response(request.getRequestId(), null, request.callDescription(), e), request);
                    }
                });
            } else {
                writeResponse(ctx, new Response(request.getRequestId(), res, request.callDescription()), request);
            }
        } catch (IllegalAccessException e) {
            logger.error("error while processing request {} object is {} method is {}", request, impl, method.toGenericString(), e);
            writeResponse(ctx, new Response(request.getRequestId(), null, request.callDescription(), e), request);
        } catch (InvocationTargetException e) {
            logger.error("error while processing request {} object is {} method is {}", request, impl, method.toGenericString(), e);
            writeResponse(ctx, new Response(request.getRequestId(), null, request.callDescription(), e.getTargetException()), request);
        } catch (Throwable e) {
            logger.error("error while processing request {} object is {} method is {}", request, impl, method.toGenericString(), e);
            writeResponse(ctx, new Response(request.getRequestId(), null, request.callDescription(), e), request);
        }
    }


    public void cancelRequest(CancelRequest request) {
        CompletableFuture future = inProgressCalls.remove(request.getUniqueId());
        if(future != null) {
            future.cancel(request.isMayInterruptIfRunning());
        }
    }

    private void trace(Request request, ChannelHandlerContext ctx) {
        Trace trace = traceMap.get(request.getMethodId());
        if (trace != null && trace.value() != TraceType.OFF) {
            if(trace.value() == TraceType.DETAILED) {
                logger.debug("{} <-- {} : {}", getTo(ctx), getFrom(ctx), request.toDetailedString());
            }else {
                logger.debug("{} <-- {} : {}", getTo(ctx), getFrom(ctx), request);
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


    private void writeResponse(ChannelHandlerContext ctx, Response response, Request request) {
        trace(ctx, response, request.getMethodId());
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
