package org.async.rmi.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

//import io.netty.handler.codec.http.cors.CorsConfig;
//import io.netty.handler.codec.http.cors.CorsHandler;

/**
 * Created by Barak Bar Orion
 * 11/14/14.
 */
public class ClassLoaderServerInitializer extends ChannelInitializer<SocketChannel> {

    private final ClassLoader cl;

    public ClassLoaderServerInitializer(ClassLoader cl) {

        this.cl = cl;
    }

    @Override
    public void initChannel(SocketChannel ch) {
//        CorsConfig corsConfig = CorsConfig.withAnyOrigin().build();
//        ChannelPipeline pipeline = ch.pipeline();
//        pipeline.addLast(new HttpResponseEncoder());
//        pipeline.addLast(new HttpRequestDecoder());
//        pipeline.addLast(new HttpObjectAggregator(65536));
//        pipeline.addLast(new ChunkedWriteHandler());
//        pipeline.addLast(new CorsHandler(corsConfig));
//        pipeline.addLast(new ClassLoaderHandler(cl));
    }
}
