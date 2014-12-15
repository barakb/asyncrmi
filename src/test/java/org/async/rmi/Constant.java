package org.async.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Barak Bar Orion
 * 12/15/14.
 */
public interface Constant<T> extends Remote {
    T getValue() throws RemoteException;
}
