package org.async.rmi;

import org.async.rmi.modules.Exporter;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.async.rmi.Util.writeAndRead;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
    public static void beforeClass() throws IOException, ClassNotFoundException {
        org.apache.log4j.BasicConfigurator.resetConfiguration();
        org.apache.log4j.BasicConfigurator.configure();
        Counter server = new CounterServer();
        Modules.getInstance().getConfiguration().setConfigurePort(0);
        exporter = Modules.getInstance().getExporter();
        proxy = (Counter) exporter.export(server);
        client = writeAndRead(proxy);

    }

    @AfterClass
    public static void afterClass() {
        exporter.unexport(proxy);
    }

    @Before
    public void setUp() throws RemoteException {
        client.reset();
    }

    @Test
    public void sync() throws Exception {
        assertThat(client.read(), is(0));
        assertThat(client.next(), is(1));
        assertThat(client.read(), is(1));
    }

    @Test(timeout=5000)
    public void async() throws Exception {
        CompletableFuture<Integer> firstReadFuture = client.asyncRead();
        CompletableFuture<Integer> firstNextFuture = client.asyncNext();
        assertThat(firstReadFuture.isDone(), is(false));
        assertThat(firstNextFuture.isDone(), is(false));
        //noinspection StatementWithEmptyBody
        while(client.getQueueSize() < 2){
            // wait until the server have those 2 messages.
        }
        client.processQueue();
        firstReadFuture.join();
        firstNextFuture.join();
    }

}
