package org.async.rmi.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.async.rmi.messages.HandshakeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Created by Barak Bar Orion
 * 12/4/14.
 */
public class ClientHandshakeHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandshakeHandler.class);

    private HandshakeManager handshakeManager;

    public ClientHandshakeHandler(UUID clientId) {
        handshakeManager = new HandshakeManager(clientId);
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
        Filters.installClientFilters(ctx, filters);
        RMIClientHandler clientHandler = ctx.pipeline().get(RMIClientHandler.class);
        clientHandler.getHandshakeCompleteFuture().complete(null);
        ctx.fireChannelActive();
        if (filters != 0) {
            logger.debug("{}: handshake done with server {}, network filters: {}, and pipeline: {}.",
                    ctx.channel().localAddress(), ctx.channel().remoteAddress(), Filters.decode(filters),
                    ctx.pipeline().names());
        }

    }
}
