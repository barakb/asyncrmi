package org.async.rmi;

import org.async.rmi.client.ClosedException;
import org.async.rmi.modules.Exporter;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;

import static org.async.rmi.Util.writeAndRead;

/**
 * Created by Barak Bar Orion
 * 29/10/14.
 */
public class CloseTest {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(CloseTest.class);

    private static Exporter exporter;
    private static Counter client;

    @BeforeClass
    public static void beforeClass() throws Exception {
        Counter server = new CounterServer();
        exporter = Modules.getInstance().getExporter();
        client = writeAndRead(server);

    }

    @AfterClass
    public static void afterClass() {
        exporter.unexport();
    }

    @Before
    public void setUp() throws RemoteException {
    }

    @Test(timeout = 5000, expected = ClosedException.class)
    public void testRead() throws RemoteException {
        client.read();
        ((Exported)client).close();
        client.read();
    }

}
