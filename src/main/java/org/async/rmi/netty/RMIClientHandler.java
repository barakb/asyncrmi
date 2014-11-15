package org.async.rmi.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.async.rmi.Modules;
import org.async.rmi.messages.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Barak Bar Orion
 * 27/10/14.
 */
@ChannelHandler.Sharable
public class RMIClientHandler extends ChannelHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(RMIClientHandler.class);

    public RMIClientHandler() {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        super.channelRead(ctx, msg);
        Response response = (Response) msg;
        Modules.getInstance().getTransport().handleResponse(response, ctx);
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // Send the first message if this handler is a client-side handler.
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }

}
