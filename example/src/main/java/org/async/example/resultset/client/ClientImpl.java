package org.async.example.resultset.client;

import org.async.example.resultset.FileResultSetInterface;
import org.async.rmi.ResultSet;
import org.async.rmi.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by Barak Bar Orion.
 */
public class ClientImpl {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(ClientImpl.class);

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        if(args.length == 0){
            logger.warn("Missing text file param");
            return;
        }
        FileResultSetInterface server = (FileResultSetInterface) Util.readFromFile(new File(new File(".."), FileResultSetInterface.SER_FILE_NAME));
        logger.info("Retrieving file " + args[0]);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try(ResultSet<Byte> rs = server.retrieve(new File(args[0]), 1024)){
            while (rs.next()) {
                baos.write(rs.get());
            }
        }catch(Exception e){
            logger.error(e.toString(), e);
        }
        logger.info("server returns {}", new String(baos.toByteArray()));
    }

}