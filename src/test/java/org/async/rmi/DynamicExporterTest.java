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

public class DynamicExporterTest {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(DynamicExporterTest.class);

    private Counter server;
    private Exporter exporter;

    @BeforeClass
    public static void beforeClass() {
    }

    @Before
    public void setUp() {
        server = new CounterServer();
        exporter = new DynamicExporter();

    }

    @Test(timeout = 5000)
    public void exportTwice() throws Exception {
        Counter proxy = exporter.export(server);
        Counter proxy2 = exporter.export(proxy);
        assertThat(proxy, is(proxy2));
    }

    @Test(timeout = 5000)
    public void invokeProxyLocally() throws Exception {
        Counter proxy = exporter.export(server);
        assertThat(proxy.next(), equalTo(1));
    }

}
