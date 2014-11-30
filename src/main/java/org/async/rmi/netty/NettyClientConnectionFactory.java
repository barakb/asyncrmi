package org.async.rmi.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import org.async.rmi.Connection;
import org.async.rmi.Factory;
import org.async.rmi.Modules;
import org.async.rmi.TimeSpan;
import org.async.rmi.client.RemoteObjectAddress;
import org.async.rmi.messages.Message;
import org.async.rmi.pool.Pool;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


/**
 * Created by Barak Bar Orion
 * 27/10/14.
 */
public class NettyClientConnectionFactory implements Factory<CompletableFuture<Connection<Message>>> {
    private final Bootstrap bootstrap;
    private final RemoteObjectAddress address;
    private Pool<Connection<Message>> pool;

    public NettyClientConnectionFactory(final EventLoopGroup group, final RemoteObjectAddress address) {
        this.address = address;
        bootstrap = new Bootstrap();
        Factory<SslContext> sslClientContextFactory = Modules.getInstance().getConfiguration().getSslClientContextFactory();
        final SslContext sslCtx = sslClientContextFactory != null ? sslClientContextFactory.create() : null;

        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        if (sslCtx != null) {
                            p.addLast(sslCtx.newHandler(ch.alloc(), address.getHost(), address.getPort()));
                        }
                        p.addLast(
                                new MessageEncoder(),
                                new MessageDecoder(),
                                new RMIClientHandler());
                    }
                });
    }

    public void setPool(Pool<Connection<Message>> pool) {
        this.pool = pool;
    }

    @Override
    public CompletableFuture<Connection<Message>> create() {
        TimeSpan clientConnectTimeout = Modules.getInstance().getConfiguration().getClientConnectTimeout();
        CompletableFuture<Connection<Message>> res = new CompletableFuture<>();
        final NettyClientConnection connection = new NettyClientConnection(bootstrap, address, pool);
        connection.connect().addListener(future -> {
            try {
                future.get(clientConnectTimeout.getTime(), clientConnectTimeout.getTimeUnit());
                res.complete(connection);
            } catch (ExecutionException e) {
                res.completeExceptionally(e.getCause());
            } catch (InterruptedException e) {
                res.completeExceptionally(e);
            }
        });
        return res;
    }

}
