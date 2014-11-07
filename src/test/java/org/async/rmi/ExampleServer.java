package org.async.rmi;

import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.rmi.RemoteException;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Barak Bar Orion
 * 05/10/14.
 */
public class ExampleServer implements Example {
    private static final Logger logger = LoggerFactory.getLogger(ExampleServer.class);

    @Override
    public String echo(String msg) throws RemoteException {
        logger.debug("Server: called echo({})", msg);
        return msg;
    }

    @Override
    public CompletableFuture<String> futuredEcho(final String msg)
            throws RemoteException {
        logger.debug("Server: futuredEcho echo({})", msg);
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
        try {
            ExampleServer server = new ExampleServer();
            Example proxy = (Example) Modules.getInstance().getExporter().export(server);
            File file = new File("ExampleServer.proxy");
            Util.serialize(Files.asByteSink(file), proxy);
            logger.info("proxy {} saved to file  {}, server is running at: {}:{}",
                    proxy, file.getAbsolutePath());
        } catch (Exception e) {
            logger.error("ExampleServer exception while exporting:", e);
        }

        File file = new File("ExampleServer.proxy");
        //noinspection UnusedDeclaration
        Example example = (Example) Util.deserialize(Files.asByteSource(file));
        String res = example.echo("foo");
        logger.info("client got: {}", res);
        res = example.echo("foo1");
        logger.info("client got: {}", res);

        CompletableFuture<String> future = example.futuredEcho("async foo");
        res = future.join();
        logger.debug("client got async res : {}", res);
    }
}
