package org.async.rmi.messages;

import java.util.Arrays;

/**
 * Created by Barak Bar Orion
 * 27/10/14.
 */
public class Request extends Message {
    private long objectId;
    private long methodId;
    private boolean oneWay;
    private Object[] params;

    private transient String methodName;
    private String implClassName;

    @SuppressWarnings("UnusedDeclaration")
    public Request() {
    }

    public Request(long requestId, long objectId, long methodId, boolean oneWay, Object[] params, String methodName, String implClassName) {
        super(requestId);
        this.objectId = objectId;
        this.methodId = methodId;
        this.oneWay = oneWay;
        this.params = params;
        this.methodName = methodName;
        this.implClassName = implClassName;
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

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    public void setImplClassName(String implClassName) {
        this.implClassName = implClassName;
    }

    public String callDescription() {
        StringBuilder sb = new StringBuilder();
        if(implClassName != null){
            sb.append(implClassName);
        }
        sb.append("::");
        if (methodName != null) {
            sb.append(methodName);
        }
        sb.append('/');
        if (params != null) {
            sb.append(String.valueOf(params.length));
        } else {
            sb.append("0");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Request [" + callDescription() + "] {" +
                "requestId=" + getRequestId() +
                ", objectId=" + objectId +
                ", methodId=" + methodId +
                ", oneWay=" + oneWay +
                ", params=" + Arrays.toString(params) +
                '}';
    }

}
