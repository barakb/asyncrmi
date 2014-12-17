package org.async.rmi.client;

import org.async.rmi.Connection;
import org.async.rmi.OneWay;
import org.async.rmi.messages.CancelRequest;
import org.async.rmi.messages.Message;
import org.async.rmi.messages.Request;
import org.async.rmi.messages.Response;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Barak Bar Orion
 * 12/17/14.
 */
public class RequestQueue {

    private final UnicastRef unicastRef;
    private Queue<RequestHolder> requests;

    public RequestQueue(UnicastRef unicastRef) {
        this.unicastRef = unicastRef;
        this.requests = new LinkedList<>();
    }

    public synchronized boolean add(Request request, OneWay oneWay, CompletableFuture<Object> userFuture) {
        if (request instanceof CancelRequest) {
            if (removeRequest(request.getRequestId())) {
                return false;
            }
        }
        requests.add(new RequestHolder(request, oneWay, userFuture));
        return true;
    }

    private boolean removeRequest(long requestId) {
        Iterator<RequestHolder> i = requests.iterator();
        while (i.hasNext()) {
            RequestHolder holder = i.next();
            if (holder.request.getRequestId() == requestId) {
                i.remove();
                return true;
            }
        }
        return false;
    }

    public synchronized void processRequest(Connection<Message> connection, Throwable throwable, CompletableFuture<Response> responseFuture) {
        RequestHolder holder = requests.remove();
        if (holder.userFuture != null && holder.userFuture.isCancelled()) {
            return;
        }
        if (throwable != null) {
            // fail to connect.
            responseFuture.completeExceptionally(throwable);
        } else {
            unicastRef.trace(holder.request, connection);
            if (holder.oneWay != null || holder.request instanceof CancelRequest) {
                connection.sendOneWay(holder.request, responseFuture);
            } else {
                connection.send(holder.request);
            }
        }
    }

    private class RequestHolder {
        Request request;
        OneWay oneWay;
        CompletableFuture<Object> userFuture;

        public RequestHolder(Request request, OneWay oneWay, CompletableFuture<Object> userFuture) {
            this.request = request;
            this.oneWay = oneWay;
            this.userFuture = userFuture;
        }
    }
}
