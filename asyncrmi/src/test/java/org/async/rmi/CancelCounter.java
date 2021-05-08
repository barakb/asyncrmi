package org.async.rmi;

import java.rmi.Remote;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Barak Bar Orion
 * 12/17/14.
 */
@SuppressWarnings("WeakerAccess")
public interface CancelCounter extends Remote {
    CompletableFuture<Void> get();
    int getFutures();
}
