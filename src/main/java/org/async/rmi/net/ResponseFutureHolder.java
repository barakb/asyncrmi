package org.async.rmi.net;

import org.async.rmi.Trace;
import org.async.rmi.messages.InvokeRequest;
import org.async.rmi.messages.Response;

import java.util.concurrent.CompletableFuture;

/**
 * Created by Barak Bar Orion
 * 11/19/14.
 */
public class ResponseFutureHolder {
    private final InvokeRequest invokeRequest;
    private final Trace trace;
    private final CompletableFuture<Response> responseFuture;

    public ResponseFutureHolder(CompletableFuture<Response> responseFuture, InvokeRequest invokeRequest, Trace trace) {
        this.responseFuture = responseFuture;
        this.invokeRequest = invokeRequest;
        this.trace = trace;
    }

    public InvokeRequest getInvokeRequest() {
        return invokeRequest;
    }

    public CompletableFuture<Response> getResponseFuture() {
        return responseFuture;
    }

    public Trace getTrace() {
        return trace;
    }
}
