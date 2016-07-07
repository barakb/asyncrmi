package org.async.rmi;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

import static org.async.rmi.Util.writeAndRead;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test cancellation of remote future.
 * Created by Barak Bar Orion
 * 12/15/14.
 */
public class CancelingRemoteFutureTest {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(CancelingRemoteFutureTest.class);

    @BeforeClass
    public static void beforeClass() throws Exception {
    }


    @Test(timeout = 5000, expected = CancellationException.class)
    public void cancelingRemoteUnResolvedFuture() throws Exception {
        CancelCounter server = new CancelCounterServer();
        CancelCounter client = writeAndRead(server);
        CompletableFuture<Void> future = client.get();
        do {
            Thread.sleep(200);
        } while (client.getFutures() == 0);
        assertThat(client.getFutures(), is(1));
        future.cancel(true);
        do {
            Thread.sleep(200);
        } while (0 < client.getFutures());
        future.get();
    }

}
