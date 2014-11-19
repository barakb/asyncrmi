package org.async.rmi.net;

import org.async.rmi.Trace;
import org.async.rmi.messages.Request;
import org.async.rmi.messages.Response;

import java.util.concurrent.CompletableFuture;

/**
 * Created by Barak Bar Orion
 * 11/19/14.
 */
public class ResponseFutureHolder {
    private final Request request;
    private final Trace trace;
    private final CompletableFuture<Response> responseFuture;

    public ResponseFutureHolder(CompletableFuture<Response> responseFuture, Request request, Trace trace) {
        this.responseFuture = responseFuture;
        this.request = request;
        this.trace = trace;
    }

    public Request getRequest() {
        return request;
    }

    public CompletableFuture<Response> getResponseFuture() {
        return responseFuture;
    }

    public Trace getTrace() {
        return trace;
    }
}
