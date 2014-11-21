package org.async.example.futures;

import java.rmi.Remote;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Created by Barak Bar Orion
 * 05/10/14.
 */
public interface FutureExample extends Remote {
    public CompletableFuture<String> echo1(String msg);
    public Future<String> echo2(String msg);
    public Future<Void> send(String msg);
    public Future<String> failed(String msg);
}
