package org.async.rmi.netty;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.async.rmi.messages.HandshakeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by Barak Bar Orion
 * 12/2/14.
 */
public class ServerHandshakeHandler extends ChannelHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ServerHandshakeHandler.class);
    private List<String> filters;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.pipeline().remove(this);
        if(!filters.isEmpty()) {
            logger.debug("sending session network filters {} to {}", filters, ctx.channel().remoteAddress());
            if(filters.contains("drop")){
                ctx.close();
                return;
            }
//            for(int i = filters.size() - 1; -1 < i; --i){
//                switch (filters.get(i)) {
//                    case "compress":
//                        ctx.pipeline().addFirst(new JdkZlibDecoder()).addFirst(new JdkZlibEncoder());
//                        break;
//                }
//            }
            ctx.writeAndFlush(new HandshakeRequest(filters));
        }
        super.channelActive(ctx);
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }
}
