package org.async.rmi;

import org.async.rmi.config.NetMap;
import org.async.rmi.config.Rule;
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
public class NetMapTest {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(ServerTLSTest.class);
    private static CounterServer server;

    @BeforeClass
    public static void beforeClass() throws Exception {
        Modules.getInstance().getConfiguration().setNetMap(new NetMap(Arrays.asList(new Rule(".*", Arrays.asList("drop"))), null));
        server = new CounterServer();

    }

    @Test(timeout = 5000, expected = ExecutionException.class)
    public void testDropUsage() throws Exception {
        Modules.getInstance().getConfiguration().setClientConnectTimeout(1, TimeUnit.SECONDS);
        Modules.getInstance().getConfiguration().setNetMap(new NetMap(Arrays.asList(new Rule(".*", Arrays.asList("drop"))), null));
        Counter client = writeAndRead(server);
        client.toUpper("foo").get();
        ((Exported) client).close();
    }

    @Test(timeout = 5000)
    public void testCompress() throws Exception {
        Modules.getInstance().getConfiguration().setClientConnectTimeout(30, TimeUnit.SECONDS);
        Modules.getInstance().getConfiguration().setNetMap(new NetMap(Arrays.asList(new Rule(".*", Arrays.asList("compress"))), null));
        Counter client = writeAndRead(server);
        client.toUpper("foo").get();
        ((Exported) client).close();
    }

    @Test
    public void testEncrypt() throws Exception {
        Modules.getInstance().getConfiguration().setClientConnectTimeout(30, TimeUnit.SECONDS);
        Modules.getInstance().getConfiguration().setNetMap(new NetMap(Arrays.asList(new Rule(".*", Arrays.asList("encrypt"))), null));
        Counter client = writeAndRead(server);
        client.toUpper("foo").get();
        ((Exported) client).close();
    }

}
