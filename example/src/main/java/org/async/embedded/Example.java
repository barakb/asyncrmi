package org.async.embedded;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Barak Bar Orion
 * 05/10/14.
 */
public interface Example extends Remote {
    String echo(String msg) throws RemoteException;
    CompletableFuture<String> futuredEcho(String msg) throws RemoteException;
}
