package org.async.rmi.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import org.async.rmi.Connection;
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


    public ChannelFuture connect() {
        channelFuture = bootstrap.connect(address.getHost(), address.getPort());
        channelFuture.addListener((ChannelFuture future) -> {
            try {
                InetSocketAddress la = (InetSocketAddress) future.sync().channel().localAddress();
                localAddress = la.getHostString() + ":" + la.getPort();
                InetSocketAddress ra = (InetSocketAddress) future.sync().channel().remoteAddress();
                remoteAddress = ra.getHostString() + ":" + ra.getPort();
                future.sync().channel().closeFuture().addListener(cf -> {
                    closed = true;
                    pool.free(NettyClientConnection.this);
                });
            } catch (Exception e) {
                //noinspection ConstantConditions
                if (!(e instanceof java.net.ConnectException)) {
                    logger.error(e.toString(), e);
                }
            }
        });
        return channelFuture;
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
