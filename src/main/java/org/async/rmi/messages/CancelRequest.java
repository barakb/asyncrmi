package org.async.rmi.messages;

import org.async.rmi.MarshalledObject;

/**
 * Created by Barak Bar Orion
 * 12/17/14.
 */
public class CancelRequest extends Request {
    private boolean mayInterruptIfRunning;

    public CancelRequest() {
    }

    public CancelRequest(Request request, boolean mayInterruptIfRunning) {
        super(request.getRequestId(), request.getObjectId(), request.getMethodId(), false, request.getParams(), request.getMethodName(), request.getImplClassName());
        this.mayInterruptIfRunning = mayInterruptIfRunning;
    }

    public boolean isMayInterruptIfRunning() {
        return mayInterruptIfRunning;
    }
}
