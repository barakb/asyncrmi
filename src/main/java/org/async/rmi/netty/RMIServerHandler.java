package org.async.rmi.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.async.rmi.Modules;
import org.async.rmi.messages.*;
import org.async.rmi.server.ObjectRef;
import org.async.rmi.server.ServerResultSetCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.util.UUID;

/**
 * Created by Barak Bar Orion
 * 28/10/14.
 */
public class RMIServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(RMIServerHandler.class);
    private UUID clientId;


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

    private void dispatch(Request request, ChannelHandlerContext ctx) {
        if(request instanceof ResultSetRequest) {
            ServerResultSetCallback serverResultSetCallback = ctx.channel().attr(ObjectRef.SERVER_RESULT_SET_CALLBACK_ATTRIBUTE_KEY).get();
            if (serverResultSetCallback != null) {
                ResultSetRequest resultSetRequest = (ResultSetRequest) request;
                if(resultSetRequest.isCloseRequest()){
                    serverResultSetCallback.onClientClosed();
                    ctx.channel().attr(ObjectRef.SERVER_RESULT_SET_CALLBACK_ATTRIBUTE_KEY).set(null);
                }else {
                    serverResultSetCallback.resume();
                }
            }
        }else if(request instanceof InvokeRequest) {
            InvokeRequest invokeRequest = (InvokeRequest) request;
            invokeRequest.setClientId(clientId);
            long objectId = invokeRequest.getObjectId();
            ObjectRef objectRef = Modules.getInstance().getObjectRepository().get(objectId);
            if (null != objectRef) {
                if (invokeRequest instanceof CancelInvokeRequest) {
                    objectRef.cancelRequest((CancelInvokeRequest) invokeRequest);
                } else {
                    objectRef.invoke(invokeRequest, ctx);
                }
            } else {
                Response response = new Response(invokeRequest.getRequestId(), null, invokeRequest.callDescription()
                        , new RemoteException("Object id [" + invokeRequest.getObjectId()
                        + "] not found, while trying to serve client request [" + request.getRequestId() + "]"));
                logger.warn("{} --> {} : {}", getFrom(ctx), getTo(ctx), response);
                ctx.writeAndFlush(response);
            }
        }else{
            logger.error("Unknown request type " + request);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ServerResultSetCallback serverResultSetCallback = ctx.channel().attr(ObjectRef.SERVER_RESULT_SET_CALLBACK_ATTRIBUTE_KEY).get();
        if(serverResultSetCallback != null){
            ctx.channel().attr(ObjectRef.SERVER_RESULT_SET_CALLBACK_ATTRIBUTE_KEY).set(null);
            serverResultSetCallback.onClientClosed();
        }
    }

    void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    private String getFrom(ChannelHandlerContext ctx) {
        return addressAsString((InetSocketAddress) ctx.channel().localAddress());
    }

    private String addressAsString(InetSocketAddress socketAddress) {
        return socketAddress.getHostString() + ":" + socketAddress.getPort();
    }

    private String getTo(ChannelHandlerContext ctx) {
        return addressAsString((InetSocketAddress) ctx.channel().remoteAddress());
    }
}
