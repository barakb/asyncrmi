package org.async.rmi.messages;

/**
 * Created by Barak Bar Orion
 * 27/10/14.
 */
public class Response extends Message {
    private Object result;
    private Throwable error;

    public Response() {
    }

    public Response(long requestId, Object result) {
        super(requestId);
        this.result = result;
    }
    public Response(long requestId, @SuppressWarnings("UnusedParameters") Object result, Throwable error) {
        super(requestId);
        this.error = error;
    }

    public Throwable getError() {
        return error;
    }

    public boolean isError(){
        return error != null;
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
