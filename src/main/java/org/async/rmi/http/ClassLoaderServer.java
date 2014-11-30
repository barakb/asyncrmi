package org.async.rmi.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.async.rmi.Modules;
import org.async.rmi.modules.Transport;
import org.async.rmi.server.LoaderHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Created by Barak Bar Orion
 * 11/14/14.
 */
public class ClassLoaderServer implements Closeable {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(ClassLoaderServer.class);
    static final int PORT = Integer.parseInt(System.getProperty("port", "8080"));
    private final Channel httpChannel;

    public ClassLoaderServer(ClassLoader classLoader) throws UnknownHostException, InterruptedException {
        String codeBase = System.getProperty("java.rmi.server.codebase", null);
        if (codeBase == null || codeBase.matches("[0-9]+")) {
            Transport transport = Modules.getInstance().getTransport();
            int port = (codeBase == null) ? 0 : Integer.valueOf(codeBase);
            httpChannel = ClassLoaderServer.run(transport.getAcceptGroup(), transport.getWorkerGroup(), port, classLoader);
            InetSocketAddress inetSocketAddress = ((ServerSocketChannel) httpChannel).localAddress();
            port = inetSocketAddress.getPort();
            String hostName = Modules.getInstance().getConfiguration().getServerHostName();
            if(hostName == null){
                hostName = InetAddress.getLocalHost().getHostName();
            }

            System.setProperty("java.rmi.server.codebase", "http://" + hostName + ":" + port + "/");
            LoaderHandler.loadCodeBaseProperty();
            logger.info("Embedded HTTP server run at {} java.rmi.server.codebase is set to {} ", inetSocketAddress, System.getProperty("java.rmi.server.codebase"));
        } else {
            httpChannel = null;
        }
    }

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
        if(Modules.getInstance().getConfiguration().getServerHostName() != null){
            return b.bind(Modules.getInstance().getConfiguration().getServerHostName(), port).sync().channel();
        }else{
            return b.bind(port).sync().channel();
        }
    }

    @Override
    public void close() throws IOException {
        if (httpChannel != null) {
            httpChannel.close();
        }
    }
}
