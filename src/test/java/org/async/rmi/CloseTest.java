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
import static org.junit.Assert.fail;

/**
 * Created by Barak Bar Orion
 * 29/10/14.
 */
public class CloseTest {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(CloseTest.class);

    private static Exporter exporter;
    private static Counter proxy;
    private static Counter client;

    @BeforeClass
    public static void beforeClass() throws Exception {
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
    }

    @Test(timeout = 5000)
    @SuppressWarnings("SpellCheckingInspection")
    public void close() throws Exception {
        Modules.getInstance().getTransport().close();
        try {
            client.read();
            fail("Should throw RemoteException");
        } catch (RemoteException ignored) {
        }
    }
}
