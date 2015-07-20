package org.async.rmi.messages;

/**
 * Created by Barak Bar Orion
 * 27/10/14.
 */
public class Response extends Message {
    protected Object result;
    protected Throwable error;
    private transient String callDescription;

    public Response() {
    }

    public Response(long requestId, Object result, String callDescription) {
        this(requestId, result, callDescription, null);
    }

    public Response(long requestId, Object result, String callDescription, Throwable error) {
        super(requestId);
        this.result = result;
        this.callDescription = callDescription;
        this.error = error;
    }

    public void setCallDescription(String callDescription) {
        this.callDescription = callDescription;
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
        if(isError()){
            return "Response [" + callDescription + "] {" +
                    "requestId=" + getRequestId() +
                    ", error=" + error +
                    '}';
        }else {
            return "Response [" + callDescription + "] {" +
                    "requestId=" + getRequestId() +
                    ", result=" + result +
                    '}';
        }
    }
}
