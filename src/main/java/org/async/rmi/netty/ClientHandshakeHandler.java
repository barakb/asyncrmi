package org.async.rmi.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.async.rmi.messages.HandshakeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Barak Bar Orion
 * 12/4/14.
 */
public class ClientHandshakeHandler extends ChannelHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandshakeHandler.class);

    private HandshakeManager handshakeManager;

    public ClientHandshakeHandler() {
        handshakeManager = new HandshakeManager();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ByteBuf msg = handshakeManager.handshakeRequest();
        ctx.writeAndFlush(msg);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ctx.pipeline().remove(this);
        ByteBuf response = (ByteBuf) msg;
        int filters = handshakeManager.verifyResponse(response);
        if(filters != 0) {
            //        todo insert filters
        }
        RMIClientHandler clientHandler = ctx.pipeline().get(RMIClientHandler.class);
        clientHandler.getHandshakeCompleteFuture().complete(null);
        ctx.fireChannelActive();
        logger.debug("handshake done with server {}, network filters: {}."
                , ctx.channel().remoteAddress(), Filters.decode(filters));

    }
}
