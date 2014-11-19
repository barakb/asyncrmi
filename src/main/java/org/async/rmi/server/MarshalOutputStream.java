package org.async.rmi.server;

import org.async.rmi.Exported;
import org.async.rmi.Modules;
import org.async.rmi.NoAutoExport;
import org.async.rmi.client.RMIInvocationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.rmi.Remote;

/**
 * Created by Barak Bar Orion
 * 11/4/14.
 */
public final class MarshalOutputStream extends ObjectOutputStream {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(MarshalOutputStream.class);


    public MarshalOutputStream(OutputStream out) throws IOException {
        super(out);
        enableReplaceObject(true);
    }

    @Override
    protected final Object replaceObject(Object obj) throws IOException {
        if (obj instanceof Remote && !(obj instanceof Exported) && !(obj instanceof RMIInvocationHandler) && isAutoExport(obj)) {
            logger.debug("Auto exporting {}", obj);
            return export((Remote) obj);
        } else {
            return obj;
        }
    }

    private boolean isAutoExport(Object obj) {
        return !obj.getClass().isAnnotationPresent(NoAutoExport.class);
    }


    @Override
    protected void annotateClass(Class<?> cl) throws IOException {
        String classAnnotation = LoaderHandler.getClassAnnotation(cl);
//        logger.info("class {}, classAnnotation {}",  cl, classAnnotation);
        writeLocation(classAnnotation);
    }

    void writeLocation(String location) throws IOException {
        writeObject(location);
    }

    private Remote export(Remote obj) throws IOException {
        try {
            return Modules.getInstance().getExporter().export(obj);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to export a remote object during marshaling", e);
            return obj;
        }
    }
}
