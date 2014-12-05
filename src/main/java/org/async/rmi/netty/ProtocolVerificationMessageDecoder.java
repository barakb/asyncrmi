package org.async.rmi.netty;

import io.netty.handler.codec.FixedLengthFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Barak Bar Orion
 * 12/4/14.
 * One decode only!
 */
public class ProtocolVerificationMessageDecoder extends FixedLengthFrameDecoder {
    private static final Logger logger = LoggerFactory.getLogger(ProtocolVerificationMessageDecoder.class);

    public ProtocolVerificationMessageDecoder() {
        super(45);
        setSingleDecode(true);
    }

//    @Override
//    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
//        Object ret = super.decode(ctx, in);
//        if (ret != null) {
//            ctx.pipeline().remove(this);
//        }
//        return ret;
//    }
}

