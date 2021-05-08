package org.async.rmi;

import org.async.rmi.modules.Exporter;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;

import static org.async.rmi.Util.writeAndRead;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by Barak Bar Orion
 * 29/10/14.
 */
public class FutureInterfaceTest {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(FutureInterfaceTest.class);

    private static Exporter exporter;
    private static Counter proxy;
    private static Counter client;

    @BeforeClass
    public static void beforeClass() throws Exception {
        Counter server = new CounterServer();
        exporter = Modules.getInstance().getExporter();
        proxy = exporter.export(server);
        client = writeAndRead(proxy);

    }

    @AfterClass
    public static void afterClass() {
        exporter.unexport(proxy);
    }

    @Before
    public void setUp() throws RemoteException {
    }

//    @Test(timeout = 5000)
    @Test()
    public void test1() throws Exception {
        assertThat(client.toUpper("foo").get(), is("FOO"));
    }

    @Test(timeout = 5000)
    public void test2() throws Exception {
        assertThat(client.toUpper(null).get(), is(nullValue()));
    }

    @Test(timeout = 5000)
    public void test3() throws Exception {
        // server.toUpperFuture returns FutureTask instead of CompletableFuture.
        assertThat(client.toUpperFuture("foo").get(), is("FOO"));
    }

    @Test(timeout = 5000)
    public void test4() throws Exception {
        // server.toUpperFuture returns null instead of CompletableFuture.
        assertThat(client.toUpperFuture(null).get(), is(nullValue()));
    }

}
