package org.async.rmi.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.AttributeKey;
import org.async.rmi.Connection;
import org.async.rmi.client.ClientResultSet;
import org.async.rmi.client.ClosedException;
import org.async.rmi.client.RemoteObjectAddress;
import org.async.rmi.messages.Message;
import org.async.rmi.messages.Response;
import org.async.rmi.pool.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Created by Barak Bar Orion
 * 27/10/14.
 */
public class NettyClientConnection implements Connection<Message> {
    private static final Logger logger = LoggerFactory.getLogger(NettyClientConnection.class);
    public static final AttributeKey<Object> ATTACH_KEY = AttributeKey.valueOf("ATTACH_KEY");
    private final Bootstrap bootstrap;
    private Pool<Connection<Message>> pool;
    private RemoteObjectAddress address;
    private ChannelFuture channelFuture;
    private volatile boolean closed = false;
    private String localAddress;
    private String remoteAddress;

    public NettyClientConnection(Bootstrap bootstrap, RemoteObjectAddress address, Pool<Connection<Message>> pool) {
        this.bootstrap = bootstrap;
        this.address = address;
        this.pool = pool;
    }


    public CompletableFuture<Connection<Message>> connect() {
        final CompletableFuture<Connection<Message>> res = new CompletableFuture<>();

        channelFuture = bootstrap.connect(address.getHost(), address.getPort());
        channelFuture.addListener((ChannelFuture future) -> {
            try {
                initAddresses(future);
                Channel channel = future.sync().channel();
                // To prevent premature message from the client to the server
                // resolve the connection future only when all handshakes are done.
                RMIClientHandler clientHandler = channel.pipeline().get(RMIClientHandler.class);
                clientHandler.getHandshakeCompleteFuture().whenComplete((aVoid, throwable) -> {
                    if (throwable != null) {
                        res.completeExceptionally(throwable);
                    } else {
                        res.complete(NettyClientConnection.this);
                    }
                });
                channel.closeFuture().addListener(cf -> {
                    closed = true;
                    pool.free(NettyClientConnection.this);
                    Object attachment = attach();
                    if(attachment != null){
                        clearAttachment();
                    }
                    if(attachment instanceof ClientResultSet){
                        ((ClientResultSet)attachment).onConnectionClosed();
                    }
                    res.completeExceptionally(new ClosedException());
                });

            } catch (Exception e) {
                //noinspection ConstantConditions
                if (!(e instanceof java.net.ConnectException)) {
                    logger.error(e.toString(), e);
                }
            }
        });
        return res;
    }

    @Override
    public void attach(Object value) throws InterruptedException {
        channelFuture.sync().channel().attr(ATTACH_KEY).set(value);
    }
    @Override
    public Object attach() throws InterruptedException {
        return channelFuture.sync().channel().attr(ATTACH_KEY).get();
    }
    @Override
    public void clearAttachment() throws InterruptedException {
        channelFuture.sync().channel().attr(ATTACH_KEY).remove();
    }

    private void initAddresses(ChannelFuture future){
        try {
            Channel channel = future.sync().channel();
            InetSocketAddress la = (InetSocketAddress) channel.localAddress();
            localAddress = la.getHostString() + ":" + la.getPort();
            InetSocketAddress ra = (InetSocketAddress) channel.remoteAddress();
            remoteAddress = ra.getHostString() + ":" + ra.getPort();
        }catch(Exception ignored){
        }
    }

    @Override
    public void free() {
        pool.free(this);
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() throws IOException {
        closed = true;
        channelFuture.addListener((ChannelFuture future) -> future.channel().close());
    }

    @Override
    public void send(Message message) {
        channelFuture.addListener((ChannelFuture future) -> future.channel().writeAndFlush(message));
    }

    @Override
    public void sendOneWay(Message message, CompletableFuture<Response> response) {
        channelFuture.addListener((ChannelFuture future) -> future.channel().writeAndFlush(message)
                .addListener((ChannelFuture future1) -> {
                            try {
                                future1.get();
                                response.complete(null);
                            } catch (ExecutionException e) {
                                response.completeExceptionally(e.getCause());
                            } catch (Exception e) {
                                response.completeExceptionally(e);
                            }
                        }
                ));
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public String getLocalAddress() {
        return localAddress;
    }
}
