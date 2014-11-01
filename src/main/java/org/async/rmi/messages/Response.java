package org.async.rmi.messages;

/**
 * Created by Barak Bar Orion
 * 27/10/14.
 */
public class Response extends Message {
    private Object result;
    private Throwable error;
    private transient String methodName;

    public Response() {
    }

    public Response(long requestId, Object result, String methodName) {
        super(requestId);
        this.result = result;
        this.methodName = methodName;
    }

    public Response(long requestId, @SuppressWarnings("UnusedParameters") Object result, Throwable error) {
        super(requestId);
        this.error = error;
    }

    public Throwable getError() {
        return error;
    }

    public boolean isError() {
        return error != null;
    }

    public Object getResult() {
        return result;
    }

    @Override
    public String toString() {
        if (methodName != null) {
            return "Response [" + methodName + "] {" +
                    "requestId=" + getRequestId() +
                    ", result=" + result +
                    ", error=" + error +
                    '}';
        } else {
            return "Response {" +
                    "requestId=" + getRequestId() +
                    ", result=" + result +
                    ", error=" + error +
                    '}';
        }
    }
}
