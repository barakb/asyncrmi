package org.async.example.dcl.server;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.async.example.dcl.EventListener;
import org.async.example.dcl.Server;
import org.async.rmi.Util;
import org.async.rmi.http.ClassLoaderServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.EventObject;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Barak Bar Orion
 * 11/14/14.
 */
public class ServerImpl implements Server {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(ServerImpl.class);


    private final List<EventListener> listeners;

    public ServerImpl() {
        listeners = new CopyOnWriteArrayList<>();
    }

    @Override
    public void addListener(EventListener listener) {
        logger.info("add listener: {} listeners are: {}", listener, listeners);
        listeners.add(listener);
    }

    @Override
    public void removeListener(EventListener listener) {
        listeners.remove(listener);
        logger.info("remove listener: {} listeners are: {}", listener, listeners);
    }

    @Override
    public void triggerEvent(EventObject event) {
        logger.info("trigger event: {} listeners are: {}", event, listeners);
        for (EventListener listener : listeners) {
            listener.onEvent(event);
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        System.setProperty("java.rmi.server.codebase", "http://localhost:8080/");
        System.setProperty("side","server");
        Server server = new ServerImpl();
        Util.writeToFile(server, new File(SER_FILE_NAME));
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Channel channel = ClassLoaderServer.run(bossGroup, workerGroup, 8080, ServerImpl.class.getClassLoader());
        int port = ((InetSocketAddress) channel.localAddress()).getPort();
        logger.debug("ClassLoaderServer running on port {}", port);
        channel.closeFuture().sync();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();

    }
}
