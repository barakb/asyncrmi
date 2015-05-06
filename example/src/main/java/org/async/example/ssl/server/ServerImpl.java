package org.async.example.ssl.server;

import org.async.example.ssl.ExportJKSToPem;
import org.async.example.ssl.Server;
import org.async.rmi.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.rmi.RemoteException;
import java.util.concurrent.CompletableFuture;

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

    @Override
    public CompletableFuture<String> asyncEcho(String msg) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error(e.toString(), e);
            }
            return msg;
        });
    }

    public static void main(String[] args) throws Exception {
        if(0 < args.length){
            ExportJKSToPem.main(args);
        }
        Server server = new ServerImpl();
        Util.writeToFile(server, new File(new File(".."), SER_FILE_NAME));
        Thread.sleep(Long.MAX_VALUE);
    }
}
