package org.async.example.futures;

import org.async.rmi.Modules;
import org.async.rmi.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * Created by Barak Bar Orion
 * 05/10/14.
 */
public class FutureExampleServer implements FutureExample {
    private static final Logger logger = LoggerFactory.getLogger(FutureExampleServer.class);

    /**
     * Java8 CompletableFuture are the easiest to use.
     */
    @Override
    public CompletableFuture<String> echo1(String msg) {
        return CompletableFuture.completedFuture(msg);
        // or if you wish to run on the global FJ pool
//        return CompletableFuture.supplyAsync(() -> msg);
    }

    /**
     * But Java 1.4 Future interface can be used as well.
     */
    @Override
    public Future<String> echo2(String msg) {
        final FutureTask<String> futureTask = new FutureTask<String>(() -> msg);
        futureTask.run(); // can be submit to an Executor
        return futureTask;
    }

    /**
     * Future can be use even when the method does not return any value,
     * It just use to ack that the request was processed.
     */
    @Override
    public Future<Void> send(String msg) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Shows how Exeception and Future are handled.
     */
    @Override
    public Future<String> failed(String msg) {
        CompletableFuture<String> res = new CompletableFuture<>();
        res.completeExceptionally(new IOException("some error"));
        return res;
    }

    public static void main(String[] args) throws Exception {
        FutureExample proxy;
        try {
            FutureExampleServer server = new FutureExampleServer();
            proxy = Util.writeAndRead(server);
        } catch (Exception e) {
            logger.error("ExampleServer exception while exporting:", e);
            return;
        }

        String res = proxy.echo1("foo").get();
        logger.info("client got: {}", res);
        res = proxy.echo2("foo1").get();
        logger.info("client got: {}", res);
        proxy.send("foo1").get();
        logger.info("client processed send foo1");
        Future<String> failed = proxy.failed("foo");
        try {
            failed.get();
        }catch(ExecutionException e){
            logger.info("failed throws ", e.getCause());
        }

        Modules.getInstance().getExporter().unexport();
        Modules.getInstance().getTransport().close();
    }
}
