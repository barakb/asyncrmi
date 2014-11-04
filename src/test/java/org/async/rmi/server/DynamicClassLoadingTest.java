package org.async.rmi.server;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * Created by Barak Bar Orion
 * 11/4/14.
 */
public class DynamicClassLoadingTest {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(DynamicClassLoadingTest.class);

    @BeforeClass
    public static void beforeClass() throws IOException {
        move("target/test-classes/org/async/rmi/server/SimpleObject.class", "target/tmp/org/async/rmi/server/");
    }


    @AfterClass
    public static void afterClass() throws IOException {
        move("target/tmp/org/async/rmi/server/SimpleObject.class", "target/test-classes/org/async/rmi/server/");
    }

    private static void move(String filePath, String to) throws IOException {
        Path fileToMovePath = Paths.get(filePath);
        File toDir = new File(to);
        //noinspection ResultOfMethodCallIgnored
        toDir.mkdirs();
        Path dirPath = Paths.get(to);
        Files.move(fileToMovePath, dirPath.resolve(fileToMovePath.getFileName()));
    }

    @Test
    public void testLoadFromURL() throws Exception {
        URL url = new File("target/tmp/").toURI().toURL();
        URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{url});
        Class<?> cls = urlClassLoader.loadClass("org.async.rmi.server.SimpleObject");
        Object obj = cls.newInstance();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MarshalOutputStream os = new MarshalOutputStream(out);
        os.writeObject(obj);
        MarshalInputStream is = new MarshalInputStream(new ByteArrayInputStream(out.toByteArray()));
        Object objRead = is.readObject();
        assertThat(objRead.getClass().getName(), is("org.async.rmi.server.SimpleObject"));
        logger.info("obj: {}", objRead);
    }

}
