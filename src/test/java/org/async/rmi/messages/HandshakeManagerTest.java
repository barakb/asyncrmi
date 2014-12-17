package org.async.rmi.messages;

import io.netty.buffer.ByteBuf;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class HandshakeManagerTest {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(HandshakeManagerTest.class);


    @Test
    public void testValidateRequest() throws Exception {

        HandshakeManager handshake = new HandshakeManager(UUID.randomUUID());
        ByteBuf request = handshake.handshakeRequest();
        ByteBuf response = handshake.verifyRequest(request, 10);
        assertThat(handshake.verifyResponse(response), is(10));

        response = handshake.verifyRequest(request, 0);
        assertThat(handshake.verifyResponse(response), is(0));

    }
}