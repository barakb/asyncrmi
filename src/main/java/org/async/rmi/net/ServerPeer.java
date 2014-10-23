package org.async.rmi.net;

/**
 * Created by Barak Bar Orion
 * 06/10/14.
 */
public interface ServerPeer extends Peer{

    String getConnectionURL();

    long getObjectId();

    long getClassLoaderId();
}
