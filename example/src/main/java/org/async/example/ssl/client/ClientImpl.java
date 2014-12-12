package org.async.example.ssl.client;

import org.async.example.ssl.Server;
import org.async.rmi.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by Barak Bar Orion
 * 12/9/14.
 */
public class ClientImpl {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(ClientImpl.class);

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Server server = (Server) Util.readFromFile(new File(new File(".."), Server.SER_FILE_NAME));
        String message = server.echo("foo");
        logger.info("server returns {}", message);
    }

}
