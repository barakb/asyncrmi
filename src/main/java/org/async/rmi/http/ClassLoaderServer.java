package org.async.rmi.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Created by Barak Bar Orion
 * 11/14/14.
 */
public class ClassLoaderServer {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(ClassLoaderServer.class);
    static final int PORT = Integer.parseInt(System.getProperty("port","8080"));

    public static void main(String[] args) throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Channel channel = run(bossGroup, workerGroup, PORT, ClassLoaderServer.class.getClassLoader());
        int port = ((InetSocketAddress) channel.localAddress()).getPort();
        logger.debug("ClassLoaderServer running on port {}", port);
        channel.closeFuture().sync();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    public static Channel run(EventLoopGroup bossGroup, EventLoopGroup workerGroup, int port, ClassLoader cl) throws InterruptedException {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ClassLoaderServerInitializer(cl));

            return b.bind(port).sync().channel();
    }
}
