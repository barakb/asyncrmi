package org.async.rmi;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Barak Bar Orion
 * 29/10/14.
 */
public class CounterServer implements Counter {

    private final AtomicInteger value = new AtomicInteger(0);

    private List<Runnable> queue = Collections.synchronizedList(new ArrayList<>());

    @Override
    public int next() throws RemoteException {
        return value.incrementAndGet();
    }

    @Override
    public int read() throws RemoteException {
        return value.intValue();
    }

    @Override
    public CompletableFuture<Integer> asyncNext() {
        final CompletableFuture<Integer> res = new CompletableFuture<>();
        queue.add(() -> res.complete(value.incrementAndGet()));
        return res;
    }

    @Override
    public CompletableFuture<Integer> asyncRead() {
        final CompletableFuture<Integer> res = new CompletableFuture<>();
        queue.add(() -> res.complete(value.intValue()));
        return res;
    }

    @Override
    public void processQueue() throws RemoteException {
        while(!queue.isEmpty()){
            queue.remove(0).run();
        }
    }

    @Override
    public void reset() throws RemoteException {
        value.set(0);
    }

    @Override
    public int getQueueSize() throws RemoteException {
        return queue.size();
    }
}
