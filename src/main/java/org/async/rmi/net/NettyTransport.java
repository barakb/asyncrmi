package org.async.rmi.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.async.rmi.Configuration;
import org.async.rmi.Modules;
import org.async.rmi.client.RemoteObjectAddress;
import org.async.rmi.client.RemoteRef;
import org.async.rmi.client.UnicastRef;
import org.async.rmi.messages.Response;
import org.async.rmi.modules.Transport;
import org.async.rmi.netty.MessageDecoder;
import org.async.rmi.netty.MessageEncoder;
import org.async.rmi.netty.RMIServerHandler;
import org.async.rmi.server.ObjectRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.rmi.Remote;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Barak Bar Orion
 * 06/10/14.
 */
public class NettyTransport implements Transport {

    private static final Logger logger = LoggerFactory.getLogger(NettyTransport.class);

    private final ConcurrentHashMap<Long, CompletableFuture<Response>> awaitingResponses = new ConcurrentHashMap<>();
    private final EventLoopGroup clientEventLoopGroup = new NioEventLoopGroup();
    private final EventLoopGroup acceptGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private AtomicLong objectIds = new AtomicLong(0);
    private AtomicBoolean severStarted = new AtomicBoolean(false);

    public NettyTransport() {
    }

    @Override
    public void addResponseFuture(final long requestId, CompletableFuture<Response> responseFuture) {
        awaitingResponses.put(requestId, responseFuture);
        responseFuture.whenComplete((response, throwable) -> awaitingResponses.remove(requestId));
    }

    @Override
    public void handleResponse(Response response) {
        CompletableFuture<Response> responseFuture = awaitingResponses.remove(response.getRequestId());
        if(responseFuture != null){
            responseFuture.complete(response);
        }else{
            logger.error("unexpected response {}", response);
        }
    }

    @Override
    public void close() throws IOException {
        //todo
    }

    @Override
    public RemoteRef export(Remote impl, Class[] remoteInterfaces, Configuration configuration) throws UnknownHostException, InterruptedException {
        if (severStarted.compareAndSet(false, true)) {
            listen(configuration);
        }
        final String address = InetAddress.getLocalHost().getHostAddress();
        ObjectRef objectRef = new ObjectRef(impl, remoteInterfaces);
        long objectId = Modules.getInstance().getObjectRepository().add(objectRef);
        RemoteObjectAddress remoteObjectAddress = new RemoteObjectAddress("rmi://" + address + ":" + configuration.getActualPort(), objectIds.getAndIncrement(), objectId);
        return createUnicastRef(remoteObjectAddress, remoteInterfaces);
    }

    private void listen(Configuration configuration) throws InterruptedException {
        ServerBootstrap b = new ServerBootstrap();
        b.group(acceptGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
//                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(
                                new MessageEncoder(),
                                new MessageDecoder(),
                                new RMIServerHandler());
                    }
                });
        int actualPort = ((InetSocketAddress)b.bind(configuration.getConfigurePort()).sync().channel().localAddress()).getPort();
        Modules.getInstance().getConfiguration().setActualPort(actualPort);
    }

    @SuppressWarnings("SpellCheckingInspection")
    private RemoteRef createUnicastRef(RemoteObjectAddress remoteObjectAddress, Class[] remoteInterfaces) {
        return new UnicastRef(remoteObjectAddress, remoteInterfaces);
    }

    @Override
    public EventLoopGroup getClientEventLoopGroup() {
        return clientEventLoopGroup;
    }
}
