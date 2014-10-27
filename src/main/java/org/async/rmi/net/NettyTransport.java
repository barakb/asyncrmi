package org.async.rmi.net;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.async.rmi.Configuration;
import org.async.rmi.client.RemoteObjectAddress;
import org.async.rmi.client.UnicastRef;
import org.async.rmi.messages.Response;
import org.async.rmi.modules.Transport;
import org.async.rmi.server.RemoteRef;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Remote;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Barak Bar Orion
 * 06/10/14.
 */
public class NettyTransport implements Transport {

    private final ConcurrentHashMap<Long, CompletableFuture<Response>> awaitingResponses = new ConcurrentHashMap<>();
    private final  EventLoopGroup clientEventLoopGroup = new NioEventLoopGroup();

    public NettyTransport() {
    }

    @Override
    public void addResponseFuture(final long requestId, CompletableFuture<Response> responseFuture) {
        awaitingResponses.put(requestId, responseFuture);
        responseFuture.whenComplete((response, throwable) -> awaitingResponses.remove(requestId));
    }

    @Override
    public ServerPeer export(Remote exported, Configuration configuration) throws UnknownHostException {
        final String address = InetAddress.getLocalHost().getHostAddress();
        final int port = 5050;
        return new ServerPeer() {
            @Override
            public String getConnectionURL() {
                return "rmi://" + address + ":" + port;
            }

            @Override
            public long getObjectId() {
                return 8;
            }

            @Override
            public long getClassLoaderId() {
                return 9;
            }
        };
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public RemoteRef createUnicastRef(RemoteObjectAddress remoteObjectAddress, Class[] remoteInterfaces) {
        return new UnicastRef(remoteObjectAddress, remoteInterfaces);
    }

    @Override
    public EventLoopGroup getClientEventLoopGroup() {
        return clientEventLoopGroup;
    }
}
