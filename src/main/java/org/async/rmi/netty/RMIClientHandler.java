package org.async.rmi.netty;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.async.rmi.Modules;
import org.async.rmi.messages.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Created by Barak Bar Orion
 * 27/10/14.
 */
public class RMIClientHandler extends ChannelHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(RMIClientHandler.class);
    private final CompletableFuture<Void> handshakeCompleteFuture;

    public RMIClientHandler() {
        handshakeCompleteFuture = new CompletableFuture<>();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Response response = (Response) msg;
        Modules.getInstance().getTransport().handleResponse(response, ctx);
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

    public CompletableFuture<Void> getHandshakeCompleteFuture() {
        return handshakeCompleteFuture;
    }
}
