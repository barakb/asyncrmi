package org.async.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Created by Barak Bar Orion
 * 29/10/14.
 */
public interface Counter extends Remote {

    int next() throws RemoteException;

    int read() throws RemoteException;

    CompletableFuture<Integer> asyncNext();

    CompletableFuture<Integer> asyncRead();

    void processQueue() throws RemoteException;

    void reset() throws RemoteException;

    int getQueueSize() throws RemoteException;

    Integer readAfterDelay(long millis);

    CompletableFuture<Integer> asyncReadAfterDelay(long millis);

    Future<String> toUpper(String msg);

    Future<String> toUpperFuture(String msg);

    @OneWay
    void sleepSlow(long time);

    @OneWay
    CompletableFuture<Void> sleepFast(long time);

    void sleepOneWayOnTheImpl(long time);

    void fastSleepOneWayOnTheImpl(long time);
}
