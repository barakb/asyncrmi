package org.async.rmi.modules;

import io.netty.channel.EventLoopGroup;
import org.async.rmi.Configuration;
import org.async.rmi.client.RemoteObjectAddress;
import org.async.rmi.messages.Response;
import org.async.rmi.net.ServerPeer;
import org.async.rmi.server.RemoteRef;

import java.net.UnknownHostException;
import java.rmi.Remote;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Barak Bar Orion
 * 12/10/14.
 */
public interface Transport {
    public ServerPeer export(Remote exported, Configuration configuration) throws UnknownHostException;

    @SuppressWarnings("SpellCheckingInspection")
    RemoteRef createUnicastRef(RemoteObjectAddress remoteObjectAddress, Class[] remoteInterfaces);

    void addResponseFuture(long requestId, CompletableFuture<Response> responseFuture);

    EventLoopGroup getClientEventLoopGroup();
}
