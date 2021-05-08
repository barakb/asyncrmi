package org.async.rmi.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.async.rmi.Modules;
import org.async.rmi.config.NetMap;
import org.async.rmi.config.Rule;
import org.async.rmi.messages.HandshakeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

/**
 * Created by Barak Bar Orion
 * 12/4/14.
 */
public class ServerHandshakeHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ServerHandshakeHandler.class);

    private HandshakeManager handshakeManager;
    private int filters;
    private Rule rule;

    public ServerHandshakeHandler() {
        handshakeManager = new HandshakeManager();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        rule = getMatchingRule(ctx);
        filters = Filters.encode(getMatchingFilters(rule));
        if (Filters.hasDrop(filters)) {
            logger.debug("drop connection to {}", ctx.channel().remoteAddress());
            ctx.channel().close();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ctx.pipeline().remove(this);
        ByteBuf reply = handshakeManager.verifyRequest((ByteBuf) msg, filters);
        ctx.pipeline().get(RMIServerHandler.class).setClientId(handshakeManager.getClientId());
        Filters.installServerFilters(ctx, filters, rule);
        ctx.writeAndFlush(reply).addListener(future -> ctx.fireChannelActive());
        if (filters != 0) {
            logger.debug("{}: handshake done with client {}, network filters: {}, and pipeline: {}",
                    ctx.channel().localAddress(), ctx.channel().remoteAddress(), Filters.decode(filters),
                    ctx.pipeline().names());
        }
    }

    private Rule getMatchingRule(ChannelHandlerContext ctx){
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        NetMap netMap = Modules.getInstance().getConfiguration().getNetMap();
        if (netMap != null) {
            for (Rule rule : netMap.getRules()) {
                if (rule.match(address.getHostName(), address.getAddress().getHostAddress())) {
                    return rule;
                }
            }
        }
        return null;
    }

    private List<String> getMatchingFilters(Rule rule) {
        if(rule != null) {
            return rule.getFilters();
        }
        return Collections.emptyList();
    }

}
