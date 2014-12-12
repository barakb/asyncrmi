package org.async.example.dcl.client;

import org.async.example.dcl.EventListener;
import org.async.example.dcl.Server;
import org.async.rmi.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by Barak Bar Orion
 * 11/14/14.
 */
public class ClientImpl {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(ClientImpl.class);

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        System.setProperty("side", "client");
        Server server = (Server) Util.readFromFile(new File(Server.SER_FILE_NAME));

        logger.info("*************************");
        logger.info("SerializableListener demo");
        logger.info("*************************");

        EventListener serializableListener = new SerializableListener(1);
        server.addListener(serializableListener);
        server.triggerEvent(new ClientEvent(ClientImpl.class, 1));
        server.removeListener(serializableListener);
        server.triggerEvent(new ClientEvent(ClientImpl.class, 1));

        logger.info("*******************");
        logger.info("RemoteListener demo");
        logger.info("*******************");
        RemoteListener remoteListener = new RemoteListener(2);

        server.addListener(remoteListener);
        server.triggerEvent(new ClientEvent(ClientImpl.class, 1));
        server.removeListener(remoteListener);
        server.triggerEvent(new ClientEvent(ClientImpl.class, 1));

        logger.info("*********************");
        logger.info("FilteredListener demo");
        logger.info("*********************");

        FilteredListener evenListener = new FilteredListener(3);
        server.addListener(evenListener);
        for (int i = 0; i < 6; ++i) {
            server.triggerEvent(new ClientEvent(ClientImpl.class, i));
        }
        logger.info("*************************");
        logger.info("FilteredListener demo end");
        logger.info("*************************");

        server.removeListener(evenListener);

        server.triggerEvent(new ClientEvent(ClientImpl.class, 6));

    }

}
