package org.async.rmi.resultset;

import org.async.rmi.ResultSet;
import org.async.rmi.ResultSetCallback;
import org.async.rmi.Trace;
import org.async.rmi.TraceType;
import org.async.rmi.server.ResultSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.rmi.RemoteException;

/**
 * Created by Barak Bar Orion
 * 7/19/15.
 */
@SuppressWarnings("WeakerAccess")
public class FileContentRetrieverServer implements FileContentRetriever {
    private static final Logger logger = LoggerFactory.getLogger(ResultSetTest.class);

    @Override
    public ResultSet<Byte> retrieve(File file, final int bufferSize) throws RemoteException {
        int bytes = 0;
        try (InputStream is = new FileInputStream(file); ResultSetCallback<Byte> callback = ResultSets.getCallback()) {
            byte[] buffer = new byte[bufferSize];
            int read;
            while ((read = is.read(buffer)) != -1) {
                Byte[] sentBuf = new Byte[read];
                for (int i = 0; i < read; ++i) {
                    sentBuf[i] = buffer[i];
                }
                bytes += read;
                if (callback.send(sentBuf)) {
                    // connection or proxy was closed.
                    logger.info("sent {} bytes",  bytes);
                    return null;
                }
            }
        } catch (Throwable t) {
            logger.error(t.toString(), t);
        }
        logger.info("sent {} bytes",  bytes);
        return null;
    }
}

