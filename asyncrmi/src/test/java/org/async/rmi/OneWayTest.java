package org.async.rmi;

import org.async.rmi.config.Configuration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.async.rmi.Util.writeAndRead;

/**
 * Created by Barak Bar Orion
 * 11/15/14.
 */
public class OneWayTest {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(OneWayTest.class);
    private static Counter client;

    @BeforeClass
    public static void beforeClass() throws Exception {
        Counter server = new CounterServer();
        Configuration configuration = Modules.getInstance().getConfiguration();
        configuration.setConfigurePort(0);
        configuration.setClientTimeout(1, TimeUnit.SECONDS);
        client = writeAndRead(server);
    }

    /**
     * one way call should not get timeout exception when the timeout is 1 second
     * and the call takes 30 seconds to finish
     * This OneWay call is "slow" because it returns void and force the RMI to return only after the message
     * was fully sent.
     *
     * @throws RemoteException
     */
    @Test(timeout = 5000)
    public void testSlowOneWay() throws RemoteException, InterruptedException {
        client.sleepSlow(30000);
    }

    @Test(timeout = 5000)
    public void testFastOneWay() throws RemoteException {
        CompletableFuture<Void> messageSent = client.sleepFast(30000);
        messageSent.thenAccept(aVoid -> logger.info("message sent"));
    }

    @Test(timeout = 5000)
    public void testOneWayOnImpl() throws RemoteException {
        client.sleepOneWayOnTheImpl(30000);
    }

    @Test(timeout = 5000)
    public void testFastSleepOneWayOnTheImpl() throws RemoteException {
        client.fastSleepOneWayOnTheImpl(30000);
    }

}
