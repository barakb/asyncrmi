package org.async.rmi.client;

import org.async.rmi.OneWay;

import java.io.IOException;
import java.rmi.Remote;

/**
 * Created by Barak Bar Orion
 * 27/10/14.
 */
public interface RemoteRef extends java.io.Externalizable {

    Object invoke(Remote obj,
                  java.lang.reflect.Method method,
                  Object[] params,
                  long opHash,
                  OneWay oneWay,
                  boolean isResultSet)
            throws Throwable;

    void close() throws IOException;
}
