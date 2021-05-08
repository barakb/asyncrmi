package org.async.rmi;

import org.async.rmi.config.Configuration;
import org.async.rmi.modules.Exporter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.async.rmi.Util.writeAndRead;

/**
 * Created by Barak Bar Orion
 * 29/10/14.
 */
public class TimeoutTest {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(TimeoutTest.class);

    private static Exporter exporter;
    private static Counter client;

    @BeforeClass
    public static void beforeClass() throws Exception {
        Counter server = new CounterServer();
        Configuration configuration = Modules.getInstance().getConfiguration();
        configuration.setConfigurePort(0);
        configuration.setClientTimeout(3, TimeUnit.SECONDS);
        exporter = Modules.getInstance().getExporter();
        client = writeAndRead(server);

    }

    @AfterClass
    public static void afterClass() {
        exporter.unexport();
    }

    @Test(timeout = 50000)
    public void testRead() throws RemoteException {
        client.read();
    }

    @Test(timeout = 5000, expected = TimeoutException.class)
    public void readTimeout() {
        client.readAfterDelay(5000);
    }

    @Test(timeout = 5000, expected = ExecutionException.class)
    public void asyncReadTimeout() throws Exception {
        client.asyncReadAfterDelay(5000).get();
    }
}
