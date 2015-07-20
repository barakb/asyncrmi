package org.async.rmi.resultset;

import org.async.rmi.Modules;
import org.async.rmi.ResultSet;
import org.async.rmi.modules.Exporter;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.rmi.RemoteException;

import static org.async.rmi.Util.writeAndRead;

/**
 * Created by Barak Bar Orion
 * 7/19/15.
 */
public class ResultSetTest {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(ResultSetTest.class);

    private static Exporter exporter;
    private static FileContentRetriever proxy;
    private static FileContentRetriever client;

    @BeforeClass
    public static void beforeClass() throws Exception {
        FileContentRetriever server = new FileContentRetrieverServer();
        Modules.getInstance().getConfiguration().setConfigurePort(0);
        exporter = Modules.getInstance().getExporter();
        proxy = exporter.export(server);
        client = writeAndRead(proxy);
    }

    @AfterClass
    public static void afterClass() {
        exporter.unexport(proxy);
    }

    @Before
    public void setUp() throws RemoteException {
    }


//    @Test(timeout = 5000)
    @Test
    public void call() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try(ResultSet<Byte> rs = client.retrieve(new File("pom.xml"), 1024)) {
            while (rs.next()) {
                baos.write(rs.get());
            }
        }
        logger.info("Done baos is {}",  new String(baos.toByteArray()));

    }


}
