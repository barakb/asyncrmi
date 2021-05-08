package org.async.rmi;

import org.async.rmi.modules.Exporter;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.async.rmi.Util.writeAndRead;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Created by Barak Bar Orion
 * 29/10/14.
 */
public class UnexportTest {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(UnexportTest.class);

    private Exporter exporter;
    private Counter proxy;
    private Counter client;

    @BeforeClass
    public static void beforeClass() throws Exception {

    }

    @AfterClass
    public static void afterClass() {
    }

    @Before
    public void setUp() throws Exception {
        Counter server = new CounterServer();
        Modules.getInstance().getConfiguration().setConfigurePort(0);
        exporter = Modules.getInstance().getExporter();
        proxy = exporter.export(server);
        client = writeAndRead(proxy);
    }

    @Test(timeout = 5000)
    @SuppressWarnings("SpellCheckingInspection")
    public void unexportProxy() throws Exception {
        exporter.unexport(proxy);
        try {
            client.read();
            fail("Should throw RemoteException");
        } catch (RemoteException ignored) {
        }
    }

    @Test(timeout = 5000)
    @SuppressWarnings("SpellCheckingInspection")
    public void unexportAll() throws Exception {
        exporter.unexport();
        try {
            client.read();
            fail("Should throw RemoteException");
        } catch (RemoteException ignored) {
        }
    }

    @Test(timeout = 5000)
    @SuppressWarnings("SpellCheckingInspection")
    public void asyncUnexportProxy() throws Exception {
        exporter.unexport(proxy);
        CompletableFuture<Integer> asyncRead = client.asyncRead();
        try {
            asyncRead.get();
            fail("Should throw RemoteException");
        } catch (ExecutionException e) {
            assertThat((RemoteException) e.getCause(), isA(RemoteException.class));
        }
    }

    @Test(timeout = 5000)
    @SuppressWarnings("SpellCheckingInspection")
    public void asyncUnexportAll() throws Exception {
        exporter.unexport();
        CompletableFuture<Integer> asyncRead = client.asyncRead();
        try {
            asyncRead.get();
            fail("Should throw RemoteException");
        } catch (ExecutionException e) {
            assertThat((RemoteException) e.getCause(), isA(RemoteException.class));
        }
    }

}
