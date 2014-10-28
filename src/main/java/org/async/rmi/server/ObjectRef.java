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
import java.rmi.Remote;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
            throw new IllegalArgumentException("Unknown method id " + request.getMethodId() + " in request " + request + " of object " + impl);
        }
        try {
            Object res = method.invoke(impl, request.getParams());
            if (res instanceof CompletableFuture) {
                //noinspection unchecked
                ((CompletableFuture<Object>) res).whenComplete((o, e) -> {
                    if (o != null) {
                        writeResponse(ctx, new Response(request.getRequestId(), o));
                    } else {
                        writeResponse(ctx, new Response(request.getMethodId(), null, e));
                    }
                });
            } else {
                writeResponse(ctx, new Response(request.getRequestId(), res));
            }
        } catch (IllegalAccessException e) {
            writeResponse(ctx, new Response(request.getMethodId(), null, e));
        } catch (InvocationTargetException e) {
            writeResponse(ctx, new Response(request.getMethodId(), null, e.getTargetException()));
        }
    }

    private void writeResponse(ChannelHandlerContext ctx, Response response) {
        logger.debug("--> {}", response);
        ctx.writeAndFlush(response);
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
