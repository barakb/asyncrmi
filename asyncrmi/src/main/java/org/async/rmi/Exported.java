package org.async.rmi;

/**
 * Created by Barak Bar Orion
 * 08/10/14.
 */
public interface Exported {
    long getObjectId();
    void close();
    void redirect(long objectId, String host, int port);
}
