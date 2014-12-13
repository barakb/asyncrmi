package org.async.example.ssl.server;

import org.async.example.ssl.Server;
import org.async.rmi.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.rmi.RemoteException;

/**
 * Created by Barak Bar Orion
 * 12/9/14.
 */
public class ServerImpl implements Server {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(ServerImpl.class);


    @Override
    public String echo(String msg) throws RemoteException {
        return msg;
    }


    public static void main(String[] args) throws Exception {
        Server server = new ServerImpl();
        Util.writeToFile(server, new File(new File(".."), SER_FILE_NAME));
        Thread.sleep(Long.MAX_VALUE);
    }
}
