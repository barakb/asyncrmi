package org.async.rmi.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.async.rmi.messages.Message;
import org.async.rmi.server.MarshalOutputStream;

/**
 * Created by Barak Bar Orion
 * 27/10/14.
 */
@ChannelHandler.Sharable
public class MessageEncoder extends MessageToByteEncoder<Message> {
    private static final byte[] LENGTH_PLACEHOLDER = new byte[4];

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        int startIdx = out.writerIndex();

        ByteBufOutputStream bout = new ByteBufOutputStream(out);
        bout.write(LENGTH_PLACEHOLDER);
        MarshalOutputStream oos = new MarshalOutputStream(bout);
        oos.writeObject(msg);
        oos.flush();
        oos.close();

        int endIdx = out.writerIndex();

        out.setInt(startIdx, endIdx - startIdx - 4);
    }
}
