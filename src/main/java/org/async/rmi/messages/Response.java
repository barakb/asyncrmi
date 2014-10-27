package org.async.rmi.messages;

/**
 * Created by Barak Bar Orion
 * 27/10/14.
 */
public class Response extends Message {
    private Object result;

    public Response() {
    }

    public Response(long requestId, Object result) {
        super(requestId);
        this.result = result;
    }


    public Object getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "Response{" +
                "requestId=" + getRequestId() +
                ", result=" + result +
                '}';
    }
}
