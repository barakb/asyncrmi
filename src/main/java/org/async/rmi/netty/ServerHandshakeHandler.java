package org.async.rmi.netty;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.async.rmi.Modules;
import org.async.rmi.Netmap;
import org.async.rmi.messages.HandshakeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

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
        List<String> filters = getMatchingFilters(address);
        for (String filter : filters) {
            applyFilter(ctx, filter);
        }
        ctx.writeAndFlush(new HandshakeRequest());
        super.channelActive(ctx);
    }

    private void applyFilter(ChannelHandlerContext ctx, String filter) {
        switch (filter) {
            case "drop":
                ctx.close();
                break;
        }
    }

    private List<String> getMatchingFilters(InetSocketAddress address) {
        Netmap netmap = Modules.getInstance().getConfiguration().getNetmap();
        if (netmap != null) {
            for (Netmap.Rule rule : netmap.getRules()) {
                if (rule.getMatch().match(address.getHostName(), address.getAddress().getHostAddress(), address.getPort())) {
                    return rule.getFilters();
                }
            }
        }
        return Collections.emptyList();
    }
}
