package org.async.rmi.net;

import org.async.rmi.messages.Request;
import org.async.rmi.messages.Response;

import java.util.concurrent.CompletableFuture;

/**
 * Created by Barak Bar Orion
 * 11/19/14.
 */
public class ResponseFutureHolder {
    private final Request request;
    private final CompletableFuture<Response> responseFuture;

    public ResponseFutureHolder(CompletableFuture<Response> responseFuture, Request request) {
        this.responseFuture = responseFuture;
        this.request = request;
    }

    public Request getRequest() {
        return request;
    }

    public CompletableFuture<Response> getResponseFuture() {
        return responseFuture;
    }
}
