package org.async.rmi.messages;

import java.util.Arrays;

/**
 * Created by Barak Bar Orion
 * 27/10/14.
 */
public class Request extends Message {
    private long objectId;
    private long methodId;
    private Object[] params;

    @SuppressWarnings("UnusedDeclaration")
    public Request() {
    }

    public Request(long requestId, long objectId, long methodId, Object[] params) {
        super(requestId);
        this.objectId = objectId;
        this.methodId = methodId;
        this.params = params;
    }


    public long getObjectId() {
        return objectId;
    }

    public long getMethodId() {
        return methodId;
    }

    public Object[] getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "Request{" +
                "requestId=" + getRequestId() +
                ", objectId=" + objectId +
                ", methodId=" + methodId +
                ", params=" + Arrays.toString(params) +
                '}';
    }
}
