package org.async.rmi;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import jdk.nashorn.internal.runtime.regexp.joni.Config;
import org.async.rmi.config.Configuration;
import org.async.rmi.config.NetMap;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.async.rmi.Util.writeAndRead;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Barak Bar Orion
 * 11/15/14.
 */
public class ServerTLSTest {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(ServerTLSTest.class);
    private static Counter client;
    private final static String CONFIG_STRING = "---\n" +
            "netMap:\n" +
            "   rules:\n" +
            "      - match: .*\n" +
            "        filters: [encrypt, compress]\n" +
            "...";


    @BeforeClass
    public static void beforeClass() throws Exception {
        SelfSignedCertificate ssc = new SelfSignedCertificate();
//        final SslContext sslServerContext = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
//        final SslContext sslClientContext = SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);
        Configuration configuration = Util.readConfiguration(CONFIG_STRING);
        Modules.getInstance().setConfiguration(configuration);

//        Modules.getInstance().getConfiguration().setSslServerContextFactory(() -> sslServerContext);
//        Modules.getInstance().getConfiguration().setSslClientContextFactory(() -> sslClientContext);

        Counter server = new CounterServer();
        client = writeAndRead(server);

    }

//    @Test(timeout = 5000)
    @Test
    public void testSSL() throws Exception{
        assertThat(client.toUpper("foo").get(), equalTo("FOO"));
    }
}
