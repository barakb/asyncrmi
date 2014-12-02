package org.async.rmi.netty;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.async.rmi.messages.HandshakeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Created by Barak Bar Orion
 * 12/2/14.
 */
public class ServerHandshakeHandler extends ChannelHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ServerHandshakeHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        logger.debug("start handshake with {} {} :{}", address.getHostName(), address.getAddress().getHostAddress(), address.getPort());
        ctx.pipeline().remove(this);
        //todo add missing handlers. zip encryption etc.
        ctx.writeAndFlush(new HandshakeRequest());
        super.channelActive(ctx);
    }
}
