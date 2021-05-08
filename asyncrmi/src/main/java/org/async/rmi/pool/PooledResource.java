package org.async.rmi.pool;

import java.io.Closeable;

/**
 * Created by Barak Bar Orion
 * 27/10/14.
 */
public interface PooledResource extends Closeable {

    /**
     * Mark this resource as free in the pool, the pool may close it if needed.
     */
    void free();

    /**
     * @return true iff this resource is already closed.
     */
    boolean isClosed();
}
