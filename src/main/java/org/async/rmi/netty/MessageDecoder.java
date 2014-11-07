package org.async.rmi.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.async.rmi.messages.Message;
import org.async.rmi.server.MarshalInputStream;

/**
 * Created by Barak Bar Orion
 * 27/10/14.
 */
public class MessageDecoder extends LengthFieldBasedFrameDecoder {


    /**
     * Creates a new decoder with the specified maximum object size.
     */
    public MessageDecoder() {
        super(1048576, 0, 4, 0, 4);
    }

    @Override
    protected Message decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }

        MarshalInputStream is = new MarshalInputStream(new ByteBufInputStream(frame));
        Message result = (Message) is.readObject();
        is.close();
        return result;
    }

    @Override
    protected ByteBuf extractFrame(ChannelHandlerContext ctx, ByteBuf buffer, int index, int length) {
        return buffer.slice(index, length);
    }
}
