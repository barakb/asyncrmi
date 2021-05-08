package org.async.rmi;

import org.async.rmi.modules.Exporter;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.concurrent.CompletableFuture;

import static org.async.rmi.Util.writeAndRead;
import static org.hamcrest.CoreMatchers.is;

/**
 * Created by Barak Bar Orion
 * 29/10/14.
 */
public class BasicCallTest {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(BasicCallTest.class);

    private static Exporter exporter;
    private static Counter proxy;
    private static Counter client;

    @BeforeClass
    public static void beforeClass() throws Exception {
        logger.info("before class start");
        Counter server = new CounterServer();
        Modules.getInstance().getConfiguration().setConfigurePort(0);
        exporter = Modules.getInstance().getExporter();
        proxy = exporter.export(server);
        client = writeAndRead(proxy);
        logger.info("before class done client is {}", client);
    }

    @AfterClass
    public static void afterClass() {
        exporter.unexport(proxy);
    }

    @Before
    public void setUp() throws RemoteException {
        client.reset();
    }


    @Test(timeout = 5000)
    public void sync() throws Exception {
        org.hamcrest.MatcherAssert.assertThat(client.read(), is(0));
    }

    @Test(timeout = 5000)
    public void async() throws Exception {
        CompletableFuture<Integer> firstReadFuture = client.asyncRead();
        CompletableFuture<Integer> firstNextFuture = client.asyncNext();
        org.hamcrest.MatcherAssert.assertThat(firstReadFuture.isDone(), is(false));
        org.hamcrest.MatcherAssert.assertThat(firstNextFuture.isDone(), is(false));
        //noinspection StatementWithEmptyBody
        while (client.getQueueSize() < 2) {
            // wait until the server have those 2 messages.
        }
        client.processQueue();
        firstReadFuture.join();
        firstNextFuture.join();
    }

}
