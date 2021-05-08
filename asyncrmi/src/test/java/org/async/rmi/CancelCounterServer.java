package org.async.rmi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Barak Bar Orion
 * 12/17/14.
 */
@SuppressWarnings("WeakerAccess")
public class CancelCounterServer implements CancelCounter {
    private volatile List<CompletableFuture<Void>> futures;

    public CancelCounterServer() {
        futures = new ArrayList<>();
    }

    //@Trace(TraceType.DETAILED)
    @Override
    public CompletableFuture<Void> get() {
        CompletableFuture<Void> res = new CompletableFuture<>();
        futures.add(res);
        res.exceptionally(throwable -> {
            if (throwable instanceof CancellationException) {
                futures.remove(res);
            }
            return null;
        });
        return res;
    }

    @Override
    public int getFutures() {
        return futures.size();
    }
}
