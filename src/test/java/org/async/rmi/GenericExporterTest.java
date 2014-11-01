package org.async.rmi;

import org.async.rmi.modules.Exporter;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GenericExporterTest {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(GenericExporterTest.class);

    private ExampleServer server;
    private Exporter exporter;

    @BeforeClass
    public static void beforeClass() {
    }

    @Before
    public void setUp() {
        server = new ExampleServer();
        exporter = new DynamicExporter();

    }

    @Test(timeout = 1000)
    public void exportTwice() throws Exception {
        Example proxy = (Example) exporter.export(server);
        Example proxy2 = (Example) exporter.export(proxy);
        assertThat(proxy, is(proxy2));
    }

    @Test(timeout = 1000)
    public void invokeProxyLocally() throws Exception {
        Example proxy = (Example) exporter.export(server);
        assertThat(proxy.echo("foo"), equalTo("foo"));
    }

}