package org.async.rmi.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
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
public class ServerHandshakeHandler extends ChannelHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ServerHandshakeHandler.class);

    private HandshakeManager handshakeManager;
    private int filters;

    public ServerHandshakeHandler() {
        handshakeManager = new HandshakeManager();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        filters = Filters.encode(getMatchingFilters((InetSocketAddress) ctx.channel().remoteAddress()));
        if (Filters.hasDrop(filters)) {
            logger.debug("drop connection to {}", ctx.channel().remoteAddress());
            ctx.channel().close();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ctx.pipeline().remove(this);
        ByteBuf reply = handshakeManager.verifyRequest((ByteBuf) msg, filters);
        Filters.installFilters(ctx, filters, false);
        ctx.writeAndFlush(reply).addListener(future -> ctx.fireChannelActive());
        if (filters != 0) {
            logger.debug("{}: handshake done with client {}, network filters: {}, and pipeline: {}",
                    ctx.channel().localAddress(), ctx.channel().remoteAddress(), Filters.decode(filters),
                    ctx.pipeline().names());
        }
    }

    private List<String> getMatchingFilters(InetSocketAddress address) {
        NetMap netMap = Modules.getInstance().getConfiguration().getNetMap();
        if (netMap != null) {
            for (Rule rule : netMap.getRules()) {
                if (rule.match(address.getHostName(), address.getAddress().getHostAddress())) {
                    return rule.getFilters();
                }
            }
        }
        return Collections.emptyList();
    }

}
