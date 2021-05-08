package org.async.rmi.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * Created by Barak Bar Orion
 * 11/4/14.
 */
public class MarshalInputStream extends ObjectInputStream {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(MarshalInputStream.class);

    public MarshalInputStream(InputStream in) throws IOException {
        super(in);
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass classDesc) throws IOException, ClassNotFoundException {
        Object annotation = readLocation();

        String className = classDesc.getName();
        ClassLoader defaultLoader = Thread.currentThread().getContextClassLoader();

        String codebase = null;
        if (annotation instanceof String) {
            codebase = (String) annotation;
        }
//        logger.info("class {}, classAnnotation {}",  className, codebase);
        try {
            return LoaderHandler.loadClass(codebase, className, defaultLoader);
        } catch (ClassNotFoundException e) {
            try {
                if (Character.isLowerCase(className.charAt(0)) && className.indexOf('.') == -1) {
                    return super.resolveClass(classDesc);
                }
            } catch (ClassNotFoundException ignored) {
            }
            throw e;
        }
    }

    private Object readLocation() throws IOException, ClassNotFoundException {
        return readObject();
    }

}
