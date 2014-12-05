package org.async.rmi.messages;

import io.netty.buffer.ByteBuf;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ProtocolVerificationHandshakeTest {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(ProtocolVerificationHandshakeTest.class);


    @Test
    public void testValidateRequest() throws Exception {

        ProtocolVerificationHandshake handshake = new ProtocolVerificationHandshake();
        ByteBuf request = handshake.handshakeRequest();
        ByteBuf response = handshake.verifyRequest(request, false);
        assertThat(handshake.verifyResponse(response), is(false));

        response = handshake.verifyRequest(request, true);
        assertThat(handshake.verifyResponse(response), is(true));

    }
}