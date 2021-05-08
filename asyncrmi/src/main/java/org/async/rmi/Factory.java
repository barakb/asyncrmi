package org.async.rmi;

/**
 * Created by Barak Bar Orion
 * 27/10/14.
 */
public interface Factory<T> {
    public T create();
}
