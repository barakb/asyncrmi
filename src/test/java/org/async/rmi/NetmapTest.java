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
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(ServerTLSTest.class);
    private static CounterServer server;

    @BeforeClass
    public static void beforeClass() throws Exception {
        Modules.getInstance().getConfiguration().setNetmap(new Netmap(Arrays.asList(new Netmap.Rule(new Netmap.Rule.Match(".*", null), Arrays.asList("drop")))));
        server = new CounterServer();

    }
    @Test(timeout = 5000, expected = ExecutionException.class)
    public void testDropUsage() throws Exception {
        Modules.getInstance().getConfiguration().setClientConnectTimeout(1, TimeUnit.SECONDS);
        Modules.getInstance().getConfiguration().setNetmap(new Netmap(Arrays.asList(new Netmap.Rule(new Netmap.Rule.Match(".*", null), Arrays.asList("drop")))));
        Counter client = writeAndRead(server);
        client.toUpper("foo").get();
        ((Exported)client).close();
    }

    @Test(timeout = 5000)
    public void testFilters() throws Exception {
        Modules.getInstance().getConfiguration().setClientConnectTimeout(30, TimeUnit.SECONDS);
        Modules.getInstance().getConfiguration().setNetmap(new Netmap(Arrays.asList(new Netmap.Rule(new Netmap.Rule.Match(".*", null), Arrays.asList("tls", "compress")))));
        Counter client = writeAndRead(server);
        client.toUpper("foo").get();
        ((Exported)client).close();
    }

}
