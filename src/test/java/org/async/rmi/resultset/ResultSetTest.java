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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;

import static org.async.rmi.Util.writeAndRead;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

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


    @Test(timeout = 5000)
    public void testReadAll() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ResultSet<Byte> rs = client.retrieve(new File("pom.xml"), 1024)) {
            while (rs.next()) {
                baos.write(rs.get());
            }
        }
        String collected = new String(baos.toByteArray());
        String content = new String(Files.readAllBytes(Paths.get("pom.xml")));
        assertThat(content, equalTo(collected));
        logger.debug("collected: {}", collected);
    }


    @Test(timeout = 5000)
    public void testOneByte() throws Exception {
        byte read = 0;
        try (ResultSet<Byte> rs = client.retrieve(new File("pom.xml"), 1024)) {
            if (rs.next()) {
                read = rs.get();
            }
            byte firstByte = Files.readAllBytes(Paths.get("pom.xml"))[0];
            logger.info("firstByte is " + firstByte);
            logger.info("read is " + read);
            assertThat(read, equalTo(firstByte));
        }
    }

    @Test(timeout = 5000)
    public void testReadAll1() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ResultSet<Byte> rs = client.retrieve(new File("pom.xml"), 1024)) {
            while (rs.next()) {
                baos.write(rs.get());
            }
        }
        String collected = new String(baos.toByteArray());
        String content = new String(Files.readAllBytes(Paths.get("pom.xml")));
        assertThat(content, equalTo(collected));
        logger.debug("collected: {}", collected);
    }

    @Test(timeout = 5000)
    public void testOneByte1() throws Exception {
        byte read = 0;
        try (ResultSet<Byte> rs = client.retrieve(new File("pom.xml"), 1024)) {
            if (rs.next()) {
                read = rs.get();
            }
        }
        byte firstByte = Files.readAllBytes(Paths.get("pom.xml"))[0];
        logger.info("firstByte is " + firstByte);
        logger.info("read is " + read);
        assertThat(read, equalTo(firstByte));
    }
}
