package org.async.rmi.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.async.rmi.Configuration;
import org.async.rmi.Modules;
import org.async.rmi.OneWay;
import org.async.rmi.client.PendingRequests;
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
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
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
    private final PendingRequests pendingRequests = new PendingRequests();

    @SuppressWarnings("FieldCanBeLocal")
    private final Timer timer = new Timer(true);

    public NettyTransport() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                pendingRequests.process();
            }
        }, 1000, 1000);
    }

    @Override
    public void addResponseFuture(final long requestId, CompletableFuture<Response> responseFuture) {
        awaitingResponses.put(requestId, responseFuture);
        responseFuture.whenComplete((response, throwable) -> awaitingResponses.remove(requestId));
        pendingRequests.add(responseFuture);
    }

    @Override
    public void handleResponse(Response response, ChannelHandlerContext ctx) {
        logger.debug("{} --> {} : {}", getLocalAddress(ctx), getRemoteAddress(ctx), response);
        CompletableFuture<Response> responseFuture = awaitingResponses.remove(response.getRequestId());
        if (responseFuture != null) {
            responseFuture.complete(response);
        } else {
            logger.error("unexpected response {}.", response);
        }
    }

   private String getLocalAddress(ChannelHandlerContext ctx){
       InetSocketAddress address = (InetSocketAddress) ctx.channel().localAddress();
       return address.getHostString() + ":" + address.getPort();
   }

   private String getRemoteAddress(ChannelHandlerContext ctx){
       InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
       return address.getHostString() + ":" + address.getPort();
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
    public RemoteRef export(Remote impl, Class[] remoteInterfaces, Configuration configuration, Map<Long, OneWay> oneWayMap) throws UnknownHostException, InterruptedException {

        final String address = InetAddress.getLocalHost().getHostAddress();
        ObjectRef objectRef = new ObjectRef(impl, remoteInterfaces, oneWayMap);
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
