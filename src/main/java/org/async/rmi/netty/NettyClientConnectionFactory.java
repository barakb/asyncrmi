package org.async.rmi.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.async.rmi.Connection;
import org.async.rmi.Factory;
import org.async.rmi.Modules;
import org.async.rmi.TimeSpan;
import org.async.rmi.client.RemoteObjectAddress;
import org.async.rmi.messages.Message;
import org.async.rmi.pool.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeoutException;


/**
 * Created by Barak Bar Orion
 * 27/10/14.
 */
public class NettyClientConnectionFactory implements Factory<CompletableFuture<Connection<Message>>> {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(NettyClientConnectionFactory.class);

    private final Bootstrap bootstrap;
    private final RemoteObjectAddress address;
    private Pool<Connection<Message>> pool;

    public NettyClientConnectionFactory(final EventLoopGroup group, final RemoteObjectAddress address, final UUID clientId) {
        this.address = address;
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(
                                new HandshakeMessageDecoder(),
                                new ClientHandshakeHandler(clientId),
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
        final NettyClientConnection connection = new NettyClientConnection(bootstrap, address, pool);
        final CompletableFuture<Connection<Message>> res = connection.connect();
        ForkJoinPool.commonPool().execute(() -> {
            try {
                res.get(clientConnectTimeout.getTime(), clientConnectTimeout.getTimeUnit());
            } catch (ExecutionException e) {
                res.completeExceptionally(e.getCause());
            } catch (InterruptedException | TimeoutException e) {
                res.completeExceptionally(e);
            }
        });
        return res;
    }

}
