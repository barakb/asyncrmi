package org.async.rmi.server;

import java.rmi.Remote;

/**
 * Created by Barak Bar Orion
 * 27/10/14.
 */
public interface RemoteRef extends java.io.Externalizable {

    Object invoke(Remote obj,
                  java.lang.reflect.Method method,
                  Object[] params,
                  long opHash)
            throws Exception;

}
