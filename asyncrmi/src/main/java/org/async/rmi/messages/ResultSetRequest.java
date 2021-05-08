package org.async.rmi.messages;

/**
 * Created by Barak Bar Orion
 * 7/20/15.
 */
public class ResultSetRequest extends Request {
    private final boolean closeRequest;

    public ResultSetRequest() {
        this(false);
    }

    public ResultSetRequest(boolean closeRequest) {
        this.closeRequest = closeRequest;
    }

    public boolean isCloseRequest() {
        return closeRequest;
    }
}
