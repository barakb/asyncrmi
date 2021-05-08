package org.async.rmi.modules;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import org.async.rmi.config.Configuration;
import org.async.rmi.OneWay;
import org.async.rmi.Trace;
import org.async.rmi.client.RemoteRef;
import org.async.rmi.messages.InvokeRequest;
import org.async.rmi.messages.Response;

import java.io.Closeable;
import java.net.UnknownHostException;
import java.rmi.Remote;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Barak Bar Orion
 * 12/10/14.
 */
public interface Transport extends Closeable {

    RemoteRef export(Remote impl, Class[] remoteInterfaces, Configuration configuration, Map<Long, OneWay> oneWayMap,
                     Set<Long> resultSetSet, Map<Long, Trace> traceMap, long objectId) throws UnknownHostException,
            InterruptedException;

    void addResponseFuture(InvokeRequest invokeRequest, CompletableFuture<Response> responseFuture, Trace trace);

    void handleResponse(Response response, ChannelHandlerContext ctx);

    EventLoopGroup getClientEventLoopGroup();

    void listen(ClassLoader cl) throws InterruptedException, UnknownHostException;

    void startClassLoaderServer(ClassLoader cl);

    EventLoopGroup getAcceptGroup();

    EventLoopGroup getWorkerGroup();
}
