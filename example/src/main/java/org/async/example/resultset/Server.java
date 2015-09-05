package org.async.example.resultset;

import org.async.rmi.Modules;
import org.async.rmi.ResultSet;
import org.async.rmi.ResultSetCallback;
import org.async.rmi.Util;
import org.async.rmi.server.ResultSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.rmi.RemoteException;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Barak Bar Orion
 * 05/10/14.
 */
public class Server implements ResultSetInterface {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    @Override
    public ResultSet<Byte> content(File file) throws IOException {
        ResultSetCallback<Byte> callback = ResultSets.getCallback();
        InputStream is = new FileInputStream(file);
        int ch;
        while((ch = is.read()) != -1){
            callback.send(new Byte[]{(byte)ch});
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        ResultSetInterface resultSetServer;
        try {
            Server server = new Server();
            resultSetServer = Util.writeAndRead(server);
            logger.info("proxy is: {}", server);
        } catch (Exception e) {
            logger.error("ExampleServer exception while exporting:", e);
            return;
        }

//        String res = resultSetServer.echo("foo");
//        logger.info("client got: {}", res);
//        res = resultSetServer.echo("foo1");
//        logger.info("client got: {}", res);
//
//        CompletableFuture<String> future = resultSetServer.futuredEcho("async foo");
//        res = future.join();
//        logger.debug("client got async res : {}", res);
        Modules.getInstance().getExporter().unexport();
        Modules.getInstance().getTransport().close();

    }
}
