package org.async.rmi.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import org.async.rmi.Connection;
import org.async.rmi.Modules;
import org.async.rmi.client.RemoteObjectAddress;
import org.async.rmi.messages.Message;
import org.async.rmi.messages.Response;
import org.async.rmi.pool.Pool;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Barak Bar Orion
 * 27/10/14.
 */
public class NettyClientConnection implements Connection<Message> {
    private final Bootstrap bootstrap;
    private Pool<Connection<Message>> pool;
    private RemoteObjectAddress address;
    private ChannelFuture channelFuture;
    private volatile boolean closed = false;

    public NettyClientConnection(Bootstrap bootstrap, RemoteObjectAddress address, Pool<Connection<Message>> pool) {
        this.bootstrap = bootstrap;
        this.address = address;
        this.pool = pool;
    }


    public ChannelFuture connect(){
        channelFuture = bootstrap.connect(address.getHost(), address.getPort());
        channelFuture.addListener((ChannelFuture future) -> future.channel().closeFuture().addListener(cf -> {
            closed = true;
            pool.free(NettyClientConnection.this);
        }));
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
    public void send(Message message, CompletableFuture<Response> response) {
        channelFuture.addListener((ChannelFuture future) -> future.channel().writeAndFlush(message));
    }
}
