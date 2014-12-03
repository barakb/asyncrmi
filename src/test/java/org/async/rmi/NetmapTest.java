package org.async.rmi;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.async.rmi.Util.writeAndRead;

/**
 * Created by Barak Bar Orion
 * 12/3/14.
 */
public class NetmapTest {
    private static final Logger logger = LoggerFactory.getLogger(ServerTLSTest.class);
    private static Counter client;

    @BeforeClass
    public static void beforeClass() throws Exception {
        Modules.getInstance().getConfiguration().setClientConnectTimeout(1, TimeUnit.SECONDS); //todo fixme
        Modules.getInstance().getConfiguration().setClientTimeout(1, TimeUnit.SECONDS);
        Modules.getInstance().getConfiguration().setNetmap(new Netmap(Arrays.asList(new Netmap.Rule(new Netmap.Rule.Match(".*", null), Arrays.asList("drop")))));
        Counter server = new CounterServer();
        client = writeAndRead(server);

    }

    @Test(timeout = 5000, expected = ExecutionException.class)
    public void testDropUsage() throws Exception {
        client.toUpper("foo").get();
    }

}
