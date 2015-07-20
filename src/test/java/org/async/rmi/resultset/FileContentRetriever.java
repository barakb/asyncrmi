package org.async.rmi.resultset;

import org.async.rmi.ResultSet;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Barak Bar Orion
 * 7/19/15.
 */
public interface FileContentRetriever extends Remote {
    ResultSet<Byte> retrieve(File file, int bufferSize) throws RemoteException;
}
