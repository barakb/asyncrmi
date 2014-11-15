package org.async.rmi.server;

import io.netty.channel.ChannelHandlerContext;
import org.async.rmi.Modules;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by Barak Bar Orion
 * 28/10/14.
 */
public class ObjectRef {

    private static final Logger logger = LoggerFactory.getLogger(ObjectRef.class);

    private final Remote impl;
    private Map<Long, Method> methodIdToMethodMap;

    public ObjectRef(Remote impl, Class[] remoteInterfaces) {
        this.impl = impl;
        this.methodIdToMethodMap = createMethodIdToMethodMap(remoteInterfaces);
    }

    public void invoke(Request request, ChannelHandlerContext ctx) {
        Method method = methodIdToMethodMap.get(request.getMethodId());
        if (method == null) {
            logger.error("Unknown method id {} in request {} of object ", request.getMethodId(), request,impl);
            writeResponse(ctx, new Response(request.getRequestId()
                    , null, new IllegalArgumentException("Unknown method id " + request.getMethodId() + " in request " + request + " of object " + impl)));
            return;
        }
        request.setMethodName(method.getName());
        logger.debug("{} <-- {} : {}", getTo(ctx), getFrom(ctx), request);
        try {
            final Object res = method.invoke(impl, request.getParams());
            if (res instanceof Future) {
                final CompletableFuture<Object> completableFuture = toCompletableFuture((Future) res);
                //noinspection unchecked
                completableFuture.whenComplete((o, e) -> {
                    if (o != null) {
                        writeResponse(ctx, new Response(request.getRequestId(), o, method.getName()));
                    } else {
                        writeResponse(ctx, new Response(request.getRequestId(), null, e));
                    }
                });
            } else {
                writeResponse(ctx, new Response(request.getRequestId(), res, method.getName()));
            }
        } catch (IllegalAccessException e) {
            logger.error("error while processing request {} object is {} method is {}", request, impl, method.toGenericString(), e);
            writeResponse(ctx, new Response(request.getRequestId(), null, e));
        } catch (InvocationTargetException e) {
            logger.error("error while processing request {} object is {} method is {}", request, impl, method.toGenericString(), e);
            writeResponse(ctx, new Response(request.getRequestId(), null, e.getTargetException()));
        } catch (Throwable e) {
            logger.error("error while processing request {} object is {} method is {}", request, impl, method.toGenericString(), e);
            writeResponse(ctx, new Response(request.getRequestId(), null, e));
        }
    }

    private CompletableFuture<Object> toCompletableFuture(Future future) {
        if (future instanceof CompletableFuture) {
            //noinspection unchecked
            return (CompletableFuture<Object>) future;
        } else {
            CompletableFuture<Object> res = new CompletableFuture<Object>();
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


    private void writeResponse(ChannelHandlerContext ctx, Response response) {
        logger.debug("{} --> {} : {}", getFrom(ctx), getTo(ctx), response);
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
