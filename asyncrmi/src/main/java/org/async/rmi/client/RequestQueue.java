package org.async.rmi.client;

import org.async.rmi.Connection;
import org.async.rmi.OneWay;
import org.async.rmi.messages.CancelInvokeRequest;
import org.async.rmi.messages.Message;
import org.async.rmi.messages.InvokeRequest;
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

    public synchronized boolean add(InvokeRequest invokeRequest, OneWay oneWay, CompletableFuture<Object> userFuture) {
        if (invokeRequest instanceof CancelInvokeRequest) {
            if (removeRequest(invokeRequest.getRequestId())) {
                return false;
            }
        }
        requests.add(new RequestHolder(invokeRequest, oneWay, userFuture));
        return true;
    }

    private boolean removeRequest(long requestId) {
        Iterator<RequestHolder> i = requests.iterator();
        while (i.hasNext()) {
            RequestHolder holder = i.next();
            if (holder.invokeRequest.getRequestId() == requestId) {
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
            unicastRef.trace(holder.invokeRequest, connection);
            if (holder.oneWay != null || holder.invokeRequest instanceof CancelInvokeRequest) {
                connection.sendOneWay(holder.invokeRequest, responseFuture);
            } else {
                connection.send(holder.invokeRequest);
            }
        }
    }

    private class RequestHolder {
        InvokeRequest invokeRequest;
        OneWay oneWay;
        CompletableFuture<Object> userFuture;

        public RequestHolder(InvokeRequest invokeRequest, OneWay oneWay, CompletableFuture<Object> userFuture) {
            this.invokeRequest = invokeRequest;
            this.oneWay = oneWay;
            this.userFuture = userFuture;
        }
    }
}
