package org.async.rmi.pool;

import org.async.rmi.Connection;
import org.async.rmi.Factory;
import org.async.rmi.messages.Message;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Barak Bar Orion
 * 27/10/14.
 */
public interface Pool<C extends PooledResource> extends Closeable {

    /**
     * Get resource from  the pool.
     * @return a free resource.
     */
    CompletableFuture<C> get();

    /**
     * Mark c as free in the pool.
     * May call to c.close() if the pool capacity exceeded.
     * @param c a PooledResource managed by this pool.
     */
    void free(C c);


    void setFactory(Factory<CompletableFuture<C>> factory);

}
