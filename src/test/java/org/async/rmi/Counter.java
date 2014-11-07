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

    public int next() throws RemoteException;

    public int read() throws RemoteException;

    public CompletableFuture<Integer> asyncNext();

    public CompletableFuture<Integer> asyncRead();

    public void processQueue() throws RemoteException;

    public void reset() throws RemoteException;

    public int getQueueSize() throws RemoteException;

    public Integer readAfterDelay(long millis);

    public CompletableFuture<Integer> asyncReadAfterDelay(long millis);

    public Future<String> toUpper(String msg);


}
