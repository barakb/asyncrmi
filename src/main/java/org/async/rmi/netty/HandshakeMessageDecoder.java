package org.async.rmi.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Barak Bar Orion
 * 12/4/14.
 * One decode only!
 */
public class HandshakeMessageDecoder extends FixedLengthFrameDecoder {
    private static final Logger logger = LoggerFactory.getLogger(HandshakeMessageDecoder.class);

    public HandshakeMessageDecoder() {
        super(29);
        setSingleDecode(true);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object ret = super.decode(ctx, in);
        if (ret != null) {
            ctx.pipeline().remove(this);
        }
        return ret;
    }
}

