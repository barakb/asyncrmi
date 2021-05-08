package org.async.rmi.messages;

/**
 * Created by Barak Bar Orion
 * 7/20/15.
 */
public class Request extends Message {
    public Request() {
    }

    public Request(long requestId) {
        super(requestId);
    }
}
