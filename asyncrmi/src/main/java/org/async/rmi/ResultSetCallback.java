package org.async.rmi;

/**
 * Created by Barak Bar Orion
 * 7/19/15.
 */
public interface ResultSetCallback<V> extends AutoCloseable {
    /** Block untill the client request more data or close the resultSet.
     * @param values the value to send back to the client
     * @return true if the resultSet is closed
     */
    boolean send(V [] values);
}
