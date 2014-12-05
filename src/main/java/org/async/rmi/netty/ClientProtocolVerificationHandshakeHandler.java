package org.async.rmi.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.async.rmi.messages.ProtocolVerificationHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Barak Bar Orion
 * 12/4/14.
 */
public class ClientProtocolVerificationHandshakeHandler extends ChannelHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ClientProtocolVerificationHandshakeHandler.class);

    private ProtocolVerificationHandshake protocolVerificationHandshake;

    public ClientProtocolVerificationHandshakeHandler() {
        protocolVerificationHandshake = new ProtocolVerificationHandshake();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ByteBuf msg = protocolVerificationHandshake.handshakeRequest();
        ctx.writeAndFlush(msg);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ctx.pipeline().remove(ProtocolVerificationMessageDecoder.class);
        ctx.pipeline().remove(this);
        ByteBuf response = (ByteBuf) msg;
        boolean hasFilters;
        try {
            hasFilters = protocolVerificationHandshake.verifyResponse(response);
        }catch(Exception e){
            logger.error(e.toString(), e);
            ctx.channel().close();
            return;
        }
        if(hasFilters){
            response.resetReaderIndex();
            ctx.writeAndFlush(response);
        }else {
            ctx.pipeline().remove(ClientHandshakeHandler.class);
            RMIClientHandler clientHandler = ctx.pipeline().get(RMIClientHandler.class);
            clientHandler.getHandshakeCompleteFuture().complete(null);
            ctx.fireChannelActive();
        }

    }
}
