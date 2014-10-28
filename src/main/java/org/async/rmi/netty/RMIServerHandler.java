package org.async.rmi.netty;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.async.rmi.Modules;
import org.async.rmi.messages.Request;
import org.async.rmi.messages.Response;
import org.async.rmi.server.ObjectRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Barak Bar Orion
 * 28/10/14.
 */
public class RMIServerHandler extends ChannelHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(RMIServerHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
//        // Echo back the received object to the client.
        Request request = (Request) msg;
        dispatch(request, ctx);
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

    private void dispatch(Request request, ChannelHandlerContext ctx) {
        long objectId = request.getObjectId();
        ObjectRef objectRef = Modules.getInstance().getObjectRepository().get(objectId);
        Response response = objectRef.invoke(request);
        ctx.writeAndFlush(response);
    }

}
