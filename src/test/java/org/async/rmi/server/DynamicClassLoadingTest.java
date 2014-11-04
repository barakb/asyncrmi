package org.async.rmi.server;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

/**
 * Created by Barak Bar Orion
 * 11/4/14.
 */
public class DynamicClassLoadingTest {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(DynamicClassLoadingTest.class);


    @Test
    public void testLoadFromURL() throws IOException, ClassNotFoundException {
        System.setProperty("java.rmi.server.codebase", "http://localhost/foo");
        LoaderHandler.loadCodeBaseProperty();
        SelectiveClassLoader selectiveClassLoader = new SelectiveClassLoader(new URL[]{});
        selectiveClassLoader.addIgnore("org.async.rmi.server.SimpleObject");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MarshalOutputStream os = new MarshalOutputStream(out);
        os.writeObject(new SimpleObject(5));
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(selectiveClassLoader);
        MarshalInputStream is = new MarshalInputStream(new ByteArrayInputStream(out.toByteArray()));
        Object obj = is.readObject();
        logger.info("obj: {}", obj);
    }

}
