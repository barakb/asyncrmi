package org.async.rmi.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import org.async.rmi.*;
import org.async.rmi.client.PendingRequests;
import org.async.rmi.client.RemoteObjectAddress;
import org.async.rmi.client.RemoteRef;
import org.async.rmi.client.UnicastRef;
import org.async.rmi.http.ClassLoaderServer;
import org.async.rmi.messages.Request;
import org.async.rmi.messages.Response;
import org.async.rmi.modules.Transport;
import org.async.rmi.netty.MessageDecoder;
import org.async.rmi.netty.MessageEncoder;
import org.async.rmi.netty.RMIServerHandler;
import org.async.rmi.netty.ServerHandshakeHandler;
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

    private final ConcurrentHashMap<Long, ResponseFutureHolder> awaitingResponses = new ConcurrentHashMap<>();
    private final EventLoopGroup clientEventLoopGroup = new NioEventLoopGroup();
    private volatile EventLoopGroup acceptGroup;
    private volatile EventLoopGroup workerGroup;
    private AtomicBoolean severStarted = new AtomicBoolean(false);
    private volatile Channel serverChannel;
    private final PendingRequests pendingRequests = new PendingRequests();
    private final AtomicBoolean serverClassLoaderStarted = new AtomicBoolean(false);
    @SuppressWarnings("FieldCanBeLocal")
    private final Timer timer = new Timer(true);
    private ClassLoaderServer classLoaderServer;

    public NettyTransport() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                pendingRequests.process();
            }
        }, 1000, 1000);
    }

    @Override
    public void addResponseFuture(final Request request, CompletableFuture<Response> responseFuture, Trace trace) {
        ResponseFutureHolder responseFutureHolder = new ResponseFutureHolder(responseFuture, request, trace);
        awaitingResponses.put(request.getRequestId(), responseFutureHolder);
        responseFuture.whenComplete((response, throwable) -> awaitingResponses.remove(request.getRequestId()));
        pendingRequests.add(responseFutureHolder);
    }

    @Override
    public void handleResponse(Response response, ChannelHandlerContext ctx) {
        ResponseFutureHolder responseFutureHolder = awaitingResponses.remove(response.getRequestId());
        if (responseFutureHolder != null) {
            CompletableFuture<Response> responseFuture = responseFutureHolder.getResponseFuture();
            response.setCallDescription(responseFutureHolder.getRequest().callDescription());
            trace(response, ctx, responseFutureHolder.getTrace());
            responseFuture.complete(response);
        } else {
            logger.error("unexpected response {} --> {} : {}.", getLocalAddress(ctx), getRemoteAddress(ctx), response);
        }
    }

    private void trace(Response response, ChannelHandlerContext ctx, Trace trace) {
        if(trace != null && trace.value() != TraceType.OFF) {
            logger.debug("{} --> {} : {}", getLocalAddress(ctx), getRemoteAddress(ctx), response);
        }
    }

    private String getLocalAddress(ChannelHandlerContext ctx) {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().localAddress();
        return address.getHostString() + ":" + address.getPort();
    }

    private String getRemoteAddress(ChannelHandlerContext ctx) {
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
                if (classLoaderServer != null) {
                    classLoaderServer.close();
                }
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
            logger.info("RMI server: {} is closed.", serverChannel.localAddress());
        }
    }

    @Override
    public RemoteRef export(Remote impl, Class[] remoteInterfaces, Configuration configuration, Map<Long, OneWay> oneWayMap, Map<Long, Trace> traceMap) throws UnknownHostException, InterruptedException {
        String address = configuration.getServerHostName();
        if(address ==  null){
            address = InetAddress.getLocalHost().getHostAddress();
        }
        final String callDescription = impl.getClass().getSimpleName() + "@" + impl.hashCode();
        ObjectRef objectRef = new ObjectRef(impl, remoteInterfaces, oneWayMap, traceMap, callDescription);
        long objectId = Modules.getInstance().getObjectRepository().add(objectRef);
        RemoteObjectAddress remoteObjectAddress = new RemoteObjectAddress("rmi://" + address + ":" + configuration.getActualPort(), objectId);
        return createUnicastRef(remoteObjectAddress, remoteInterfaces, objectId, traceMap, callDescription);
    }

    @Override
    public void listen(ClassLoader cl) throws InterruptedException, UnknownHostException {
        if (severStarted.compareAndSet(false, true)) {
            acceptGroup = getAcceptGroup();
            workerGroup = getWorkerGroup();
            Configuration configuration = Modules.getInstance().getConfiguration();
            final SslContext sslCtx = configuration.getSslServerContextFactory() != null ? configuration.getSslServerContextFactory().create() : null;
            ServerBootstrap b = new ServerBootstrap();
            b.group(acceptGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            if(sslCtx != null){
                                p.addLast(sslCtx.newHandler(ch.alloc()));
                            }
                            p.addLast(
                                    new MessageEncoder(),
                                    new MessageDecoder(),
                                    new ServerHandshakeHandler(),
                                    new RMIServerHandler());
                        }
                    });
            String hostName = configuration.getServerHostName();
            if(hostName == null) {
                serverChannel = b.bind(configuration.getConfigurePort()).sync().channel();
            }else{
                serverChannel = b.bind(hostName, configuration.getConfigurePort()).sync().channel();
            }
            logger.info("RMI server started: {}.", serverChannel.localAddress());
            int actualPort = ((InetSocketAddress) serverChannel.localAddress()).getPort();
            configuration.setActualPort(actualPort);
            startClassLoaderServer(cl);
        }
    }

    public void startClassLoaderServer(ClassLoader cl) {
        if (serverClassLoaderStarted.compareAndSet(false, true)) {
            try {
                acceptGroup = getAcceptGroup();
                workerGroup = getWorkerGroup();
                classLoaderServer = new ClassLoaderServer(cl);
            } catch (Exception e) {
                logger.error("Failed to run internal http class loader server", e);
            }
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    private RemoteRef createUnicastRef(RemoteObjectAddress remoteObjectAddress
            , Class[] remoteInterfaces, long objectid, Map<Long, Trace> traceMap, String callDescription) {
        return new UnicastRef(remoteObjectAddress, remoteInterfaces, objectid, traceMap, callDescription);
    }

    @Override
    public EventLoopGroup getClientEventLoopGroup() {
        return clientEventLoopGroup;
    }

    @Override
    public synchronized EventLoopGroup getAcceptGroup() {
        if (acceptGroup == null) {
            acceptGroup = new NioEventLoopGroup(1);
        }
        return acceptGroup;
    }

    @Override
    public synchronized EventLoopGroup getWorkerGroup() {
        if (workerGroup == null) {
            workerGroup = new NioEventLoopGroup();
        }
        return workerGroup;
    }
}
