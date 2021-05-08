package org.async.rmi.messages;

import java.io.Serializable;

/**
 * Created by Barak Bar Orion
 * 27/10/14.
 */
public class Message implements Serializable {
    private long requestId;

    public Message() {
    }

    public Message(long requestId) {
        this.requestId = requestId;
    }

    public long getRequestId() {
        return requestId;
    }
}
