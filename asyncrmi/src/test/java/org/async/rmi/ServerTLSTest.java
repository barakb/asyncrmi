package org.async.rmi;

import org.async.rmi.config.Configuration;
import org.async.rmi.modules.Exporter;
import org.junit.AfterClass;
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
  private static Exporter exporter;
  private static Counter proxy;


  @BeforeClass
  public static void beforeClass() throws Exception {
    Configuration configuration = Util.readConfiguration(CONFIG_STRING);
    Modules.getInstance().setConfiguration(configuration);
    Counter server = new CounterServer();
    exporter = Modules.getInstance().getExporter();
    proxy = exporter.export(server);
    client = writeAndRead(proxy);
  }

  @AfterClass
  public static void afterClass() {
    exporter.unexport(proxy);
  }

  @Test(timeout = 5000)
  public void testSSL() throws Exception {
    assertThat(client.toUpper("foo").get(), equalTo("FOO"));
  }
}
