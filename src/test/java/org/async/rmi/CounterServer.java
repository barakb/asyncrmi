package org.async.rmi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Barak Bar Orion
 * 29/10/14.
 */
public class CounterServer implements Counter {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(TimeoutTest.class);

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
        while (!queue.isEmpty()) {
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

    @Override
    public Integer readAfterDelay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception ignored) {

        }
        return value.get();
    }

    @Override
    public CompletableFuture<Integer> asyncReadAfterDelay(long millis) {
        CompletableFuture<Integer> res = new CompletableFuture<>();
        new Thread(() -> res.complete(readAfterDelay(millis))).start();
        return res;
    }

    @Override
    public Future<String> toUpper(String msg) {
        if (null == msg) {
            return CompletableFuture.completedFuture(null);
        } else {
            return CompletableFuture.supplyAsync(msg::toUpperCase);
        }
    }
    @Override
    public Future<String> toUpperFuture(String msg) {
        if (null == msg) {
            return null;
        } else {
            FutureTask<String> res = new FutureTask<>(msg::toUpperCase);
            new Thread(){
                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    res.run();
                }
            }.start();
            return res;
        }
    }

    @Override
    public void sleepSlow(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            logger.error(e.toString(), e);
        }
    }

    @Override
    public CompletableFuture<Void> sleepFast(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            logger.error(e.toString(), e);
        }
        return null;
    }

    @OneWay
    @Override
    public void sleepOneWayOnTheImpl(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            logger.error(e.toString(), e);
        }
    }

    @OneWay(full =true)
    @Override
    public void fastSleepOneWayOnTheImpl(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            logger.error(e.toString(), e);
        }
    }
}
