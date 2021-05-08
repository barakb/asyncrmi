package org.async.rmi.messages;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Barak Bar Orion
 * 12/2/14.
 */
public class HandshakeRequest extends Message {
    private List<String> attributes;

    public HandshakeRequest() {
    }

    public HandshakeRequest(List<String> attributes) {
        this.attributes = attributes;
    }
    public HandshakeRequest(String ... attributes) {
        this.attributes = Arrays.asList(attributes);
    }

    public List<String> getAttributes() {
        return attributes;
    }
}
