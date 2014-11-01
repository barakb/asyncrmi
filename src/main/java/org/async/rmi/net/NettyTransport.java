package org.async.rmi.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
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

/**
 * Created by Barak Bar Orion
 * 06/10/14.
 */
public class NettyTransport implements Transport {

    private static final Logger logger = LoggerFactory.getLogger(NettyTransport.class);

    private final ConcurrentHashMap<Long, CompletableFuture<Response>> awaitingResponses = new ConcurrentHashMap<>();
    private final EventLoopGroup clientEventLoopGroup = new NioEventLoopGroup();
    private volatile EventLoopGroup acceptGroup;
    private volatile EventLoopGroup workerGroup;
    private AtomicBoolean severStarted = new AtomicBoolean(false);
    private volatile Channel serverChannel;

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
        if (responseFuture != null) {
            responseFuture.complete(response);
        } else {
            logger.error("unexpected response {}.", response);
        }
    }

    @Override
    public void close() throws IOException {
        if (serverChannel != null) {
            try {
                serverChannel.close().channel().closeFuture().sync();
                acceptGroup.shutdownGracefully();
                acceptGroup = null;
                workerGroup.shutdownGracefully();
                workerGroup = null;
                severStarted.set(false);
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
            logger.info("RMI server: {} is closed.", serverChannel.localAddress());
        }
    }

    @Override
    public RemoteRef export(Remote impl, Class[] remoteInterfaces, Configuration configuration) throws UnknownHostException, InterruptedException {
        final String address = InetAddress.getLocalHost().getHostAddress();
        ObjectRef objectRef = new ObjectRef(impl, remoteInterfaces);
        long objectId = Modules.getInstance().getObjectRepository().add(objectRef);
        RemoteObjectAddress remoteObjectAddress = new RemoteObjectAddress("rmi://" + address + ":" + configuration.getActualPort(), objectId);
        return createUnicastRef(remoteObjectAddress, remoteInterfaces, objectId);
    }

    @Override
    public void listen() throws InterruptedException {
        if (severStarted.compareAndSet(false, true)) {
            if (acceptGroup == null) {
                acceptGroup = new NioEventLoopGroup(1);
            }
            if (workerGroup == null) {
                workerGroup = new NioEventLoopGroup();
            }
            Configuration configuration = Modules.getInstance().getConfiguration();
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
            serverChannel = b.bind(configuration.getConfigurePort()).sync().channel();
            logger.info("RMI server started: {}.", serverChannel.localAddress());
            int actualPort = ((InetSocketAddress) serverChannel.localAddress()).getPort();
            configuration.setActualPort(actualPort);
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    private RemoteRef createUnicastRef(RemoteObjectAddress remoteObjectAddress, Class[] remoteInterfaces, long objectid) {
        return new UnicastRef(remoteObjectAddress, remoteInterfaces, objectid);
    }

    @Override
    public EventLoopGroup getClientEventLoopGroup() {
        return clientEventLoopGroup;
    }
}
