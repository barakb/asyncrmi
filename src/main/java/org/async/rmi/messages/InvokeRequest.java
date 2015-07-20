package org.async.rmi.messages;

import org.async.rmi.MarshalledObject;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by Barak Bar Orion
 * 27/10/14.
 */
public class InvokeRequest extends Request {
    private long objectId;
    private long methodId;
    private boolean oneWay;
    private MarshalledObject[] params;

    private transient String methodName;
    private String implClassName;
    private transient UUID clientId;

    @SuppressWarnings("UnusedDeclaration")
    public InvokeRequest() {
    }

    public InvokeRequest(long requestId, long objectId, long methodId, boolean oneWay, MarshalledObject[] params, String methodName, String implClassName) {
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

    public MarshalledObject[] getParams() {
        return params;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setImplClassName(String implClassName) {
        this.implClassName = implClassName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getImplClassName() {
        return implClassName;
    }

    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }
    public String getUniqueId(){
        return clientId.toString() + ":" + getRequestId();
    }

    public String callDescription() {
        StringBuilder sb = new StringBuilder();
        if (implClassName != null) {
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
        return "InvokeRequest [" + callDescription() + "] {" +
                "requestId=" + getRequestId() +
                ", objectId=" + objectId +
                ", oneWay=" + oneWay +
                '}';
    }

    public String toDetailedString() {
        List params = (this.params == null) ? null : Arrays.stream(this.params).map(marshalledObject -> {
            try {
                return marshalledObject.get();
            } catch (Exception e) {
                return marshalledObject;
            }
        }).collect(Collectors.toList());
        return "InvokeRequest [" + callDescription() + "] {" +
                "requestId=" + getRequestId() +
                ", objectId=" + objectId +
                ", oneWay=" + oneWay +
                ", params=" + params +
                '}';

    }
}
