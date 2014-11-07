package org.async.rmi.netty;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.async.rmi.Modules;
import org.async.rmi.messages.Request;
import org.async.rmi.messages.Response;
import org.async.rmi.server.ObjectRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;

/**
 * Created by Barak Bar Orion
 * 28/10/14.
 */
public class RMIServerHandler extends ChannelHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(RMIServerHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
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

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        super.close(ctx, promise);
        logger.info("RMI Server is closing");
    }

    private void dispatch(Request request, ChannelHandlerContext ctx){
        long objectId = request.getObjectId();
        ObjectRef objectRef = Modules.getInstance().getObjectRepository().get(objectId);
        if(null != objectRef) {
            objectRef.invoke(request, ctx);
        }else{
            Response response = new Response(request.getRequestId(), null, new RemoteException("Object id [" + request.getObjectId() +   "] not found, while trying to serve client request [" + request.getRequestId() + "]"));
            logger.debug("--> {}", response);
            ctx.writeAndFlush(response);
        }
    }

}
