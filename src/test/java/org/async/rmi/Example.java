package org.async.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Barak Bar Orion
 * 05/10/14.
 */
public interface Example extends Remote {
    public String echo(String msg) throws RemoteException;
}
