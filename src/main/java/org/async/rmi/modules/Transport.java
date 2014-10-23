package org.async.rmi.modules;

import org.async.rmi.Configuration;
import org.async.rmi.net.ServerPeer;

import java.net.UnknownHostException;
import java.rmi.Remote;

/**
 * Created by Barak Bar Orion
 * 12/10/14.
 */
public interface Transport {
    public ServerPeer export(Remote exported, Configuration configuration) throws UnknownHostException;

}
