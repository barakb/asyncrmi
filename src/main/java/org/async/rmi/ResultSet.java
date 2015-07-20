package org.async.rmi;

/**
 * Created by Barak Bar Orion
 * 7/19/15.
 */
public interface ResultSet<V> extends AutoCloseable {
    boolean next();
    V get();
}
