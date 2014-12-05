package org.async.rmi.messages;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static io.netty.buffer.Unpooled.buffer;
import static io.netty.buffer.Unpooled.wrappedBuffer;
/**
 * Created by Barak Bar Orion
 * 12/4/14.
 */
public class ProtocolVerificationHandshake {
    public final static ByteBuf HANDSHAKE_PREFIX = wrappedBuffer(new byte []{97, 115, 121, 110, 99, 114 ,109, 105});
    private int challenge;


    /**
     * Called by the client to initiate protocol handshake.
     * @return the handshake initial message of the server.
     * @throws NoSuchAlgorithmException
     */
    public ByteBuf handshakeRequest() throws NoSuchAlgorithmException {
        challenge = (int)(Math.random() * Integer.MAX_VALUE);
        ByteBuf buffer =  buffer(45);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.writeBytes(HANDSHAKE_PREFIX);
        HANDSHAKE_PREFIX.resetReaderIndex();
        buffer.writeInt(challenge);
        buffer.writeByte(0);
        seal(buffer);
        return buffer;
    }

    /**
     * Called by the Server to verify client handshake initial message.
     * @param request the client message
     * @param hasFilters true if the server need to add filters on the channel to this server.
     * @return The message that should be return to the client (the response).
     * @throws NoSuchAlgorithmException
     * @throws IOException if the verification fail.
     */
    public ByteBuf verifyRequest(ByteBuf request, boolean hasFilters) throws NoSuchAlgorithmException, IOException {
        request.order(ByteOrder.BIG_ENDIAN);
        if(!request.slice(0, 8).equals(HANDSHAKE_PREFIX)){
            throw new IOException("Invalid protocol handshake request, prefix is not match");
        }
        int random = request.getInt(8);
        verifySeal(request);
        ByteBuf reply =  buffer(45);
        reply.order(ByteOrder.BIG_ENDIAN);
        reply.writeBytes(HANDSHAKE_PREFIX);
        HANDSHAKE_PREFIX.resetReaderIndex();
        reply.writeInt(random + 1);
        reply.writeByte(hasFilters ? 1 : 0);
        seal(reply);
        return reply;
    }

    /**
     * Called by the client to verify that the server response is according to the protocol.
     * @param response the response from the server.
     * @return true if the this pipeline should be changed (adding more filters)
     * if it true the client should wait for a message of type HandshakeRequest.
     */
    public boolean verifyResponse(ByteBuf response) throws IOException, NoSuchAlgorithmException {
        response.order(ByteOrder.BIG_ENDIAN);
        if(!response.slice(0, 8).equals(HANDSHAKE_PREFIX)){
            throw new IOException("Invalid protocol handshake request, prefix is not match");
        }
        int challenge = response.getInt(8);
        if(challenge != this.challenge + 1){
            throw new IOException("Invalid protocol handshake response, number is not match");

        }
        verifySeal(response);
        return response.getByte(12) != (byte)0;
    }


    private void verifySeal(ByteBuf request) throws NoSuchAlgorithmException, IOException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(request.nioBuffer(0, 13));
        if(!wrappedBuffer(messageDigest.digest()).equals(request.slice(13, 32))){
            throw new IOException("Invalid protocol handshake response, SHA-256 is not match");
        }
    }

    private void seal(ByteBuf buffer) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(buffer.nioBuffer(0, 13));
        buffer.writeBytes(messageDigest.digest());
    }

}
