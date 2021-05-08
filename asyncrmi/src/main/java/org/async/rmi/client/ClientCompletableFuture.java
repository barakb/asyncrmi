package org.async.rmi.client;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Created by Barak Bar Orion
 * 12/18/14.
 */
public class ClientCompletableFuture<T> extends CompletableFuture<T> {
    private final Consumer<Boolean> sendCancelRequest;

    public ClientCompletableFuture(Consumer<Boolean> sendCancelRequest) {
        super();
        this.sendCancelRequest = sendCancelRequest;
    }

    @Override
    public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        boolean res = super.cancel(mayInterruptIfRunning);
        if (res) {
            sendCancelRequest.accept(mayInterruptIfRunning);
        }
        return res;
    }
}
