package org.async.example.resultset;

import org.async.rmi.ResultSet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Barak Bar Orion
 * 05/10/14.
 */
public interface ResultSetInterface extends Remote {
    ResultSet<Byte> content(File file) throws IOException;
}
