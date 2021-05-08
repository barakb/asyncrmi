package org.async.rmi;

import org.async.rmi.config.Configuration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.async.rmi.Util.writeAndRead;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test redirect of One client to another address.
 * Created by Barak Bar Orion
 * 12/15/14.
 */
public class RedirectTest {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(RedirectTest.class);
    @SuppressWarnings("FieldCanBeLocal")
    private static Constant<String> client;

    @BeforeClass
    public static void beforeClass() throws Exception {

    }

    @Test(timeout = 5000)
    public void testRedirect() throws Exception {
        Constant<String> server = new ConstantServer<>("constant");
        Configuration configuration = Modules.getInstance().getConfiguration();
        configuration.setConfigurePort(0);
        client = writeAndRead(server);
        Constant<String> fixedServer = new ConstantServer<>("fixed");
        Modules.getInstance().getExporter().export(fixedServer, -1);
        ((Exported) client).redirect(-1, "localhost", configuration.getActualPort());
        assertThat(client.getValue(), equalTo("fixed"));
    }

}
