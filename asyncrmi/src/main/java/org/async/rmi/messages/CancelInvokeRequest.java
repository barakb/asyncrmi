package org.async.rmi.messages;

/**
 * Created by Barak Bar Orion
 * 12/17/14.
 */
public class CancelInvokeRequest extends InvokeRequest {
    private boolean mayInterruptIfRunning;

    public CancelInvokeRequest() {
    }

    public CancelInvokeRequest(InvokeRequest invokeRequest, boolean mayInterruptIfRunning) {
        super(invokeRequest.getRequestId(), invokeRequest.getObjectId(), invokeRequest.getMethodId(), false, invokeRequest.getParams(), invokeRequest.getMethodName(), invokeRequest.getImplClassName());
        this.mayInterruptIfRunning = mayInterruptIfRunning;
    }

    public boolean isMayInterruptIfRunning() {
        return mayInterruptIfRunning;
    }
}
