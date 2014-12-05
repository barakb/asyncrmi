package org.async.rmi.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.async.rmi.Modules;
import org.async.rmi.Netmap;
import org.async.rmi.messages.ProtocolVerificationHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

/**
 * Created by Barak Bar Orion
 * 12/4/14.
 */
public class ServerProtocolVerificationHandshakeHandler extends ChannelHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ServerProtocolVerificationHandshakeHandler.class);

    private ProtocolVerificationHandshake protocolVerificationHandshake;
    private List<String> filters;
    private boolean firstMessage = true;

    public ServerProtocolVerificationHandshakeHandler() {
        protocolVerificationHandshake = new ProtocolVerificationHandshake();
        filters = Collections.emptyList();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        filters = getMatchingFilters((InetSocketAddress) ctx.channel().remoteAddress());
        if(filters.isEmpty()){
            ctx.pipeline().remove(ServerHandshakeHandler.class);
        }else{
            ctx.pipeline().get(ServerHandshakeHandler.class).setFilters(filters);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        logger.info("received protocol handshake request {}.", new String(getEncoder().encode(buf.nioBuffer())));
        if(firstMessage){
            reply(ctx, msg);
        }else{
            ctx.pipeline().remove(ProtocolVerificationMessageDecoder.class);
            ctx.pipeline().remove(this);
            ctx.fireChannelActive();
        }

//        logger.debug("server protocol verification handshake with {} ended successfully.\n - filters: {}.\n - network pipeline: {}."
//                , ctx.channel().remoteAddress(), filters, ctx.pipeline().names());
    }

    private void reply(ChannelHandlerContext ctx, Object msg) {
        firstMessage = false;
        if(filters.isEmpty()) {
            ctx.pipeline().remove(ProtocolVerificationMessageDecoder.class);
            ctx.pipeline().remove(this);
        }
        ByteBuf reply;
        try {
            reply = protocolVerificationHandshake.verifyRequest((ByteBuf) msg, !filters.isEmpty());
        }catch(Exception e){
            logger.error(e.toString(), e);
            ctx.channel().close();
            return;
        }
        if(filters.isEmpty()) {
            ctx.writeAndFlush(reply)
                    .addListener(future -> ctx.fireChannelActive());
        }else{
            ctx.writeAndFlush(reply);
        }
    }

    private List<String> getMatchingFilters(InetSocketAddress address) {
        Netmap netmap = Modules.getInstance().getConfiguration().getNetmap();
        if (netmap != null) {
            for (Netmap.Rule rule : netmap.getRules()) {
                if (rule.getMatch().match(address.getHostName(), address.getAddress().getHostAddress())) {
                    return rule.getFilters();
                }
            }
        }
        return Collections.emptyList();
    }


}
