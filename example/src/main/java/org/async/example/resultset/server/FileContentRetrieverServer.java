package org.async.example.resultset.server;

import org.async.example.resultset.FileResultSetInterface;
import org.async.rmi.*;
import org.async.rmi.server.ResultSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.rmi.RemoteException;

/**
 * Created by Barak Bar Orion
 * 05/10/14.
 */
public class FileContentRetrieverServer implements FileResultSetInterface {
    private static final Logger logger = LoggerFactory.getLogger(FileContentRetrieverServer.class);


    @Trace(TraceType.DETAILED)
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

    public static void main(String[] args) throws Exception {
        try {
            FileContentRetrieverServer fileResultSetServer = new FileContentRetrieverServer();
            Util.writeToFile(fileResultSetServer, new File(new File(".."), SER_FILE_NAME));
            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            logger.error("ExampleServer exception while exporting:", e);
        }
    }
}
