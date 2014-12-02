package org.async.rmi.netty;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by Barak Bar Orion
 * 12/2/14.
 */
public class ClientHandshakeHandler extends ChannelHandlerAdapter {

    public ClientHandshakeHandler() {
    }



    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //swallow until handshake is done.
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ctx.pipeline().remove(this);
        //todo add missing handlers. zip encryption etc.
        super.channelActive(ctx);
    }
}
