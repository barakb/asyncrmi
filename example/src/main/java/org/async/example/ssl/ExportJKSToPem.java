package org.async.example.ssl;

import org.async.rmi.tls.JKS;
import org.async.rmi.tls.TLSUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * A utility class to export the certificate and private keys to pem format.
 * Created by Barak Bar Orion
 * 12/13/14.
 */
public class ExportJKSToPem {
    private static final Logger logger = LoggerFactory.getLogger(ExportJKSToPem.class);

    public static void main(String[] args) throws Exception{
        JKS jks = new JKS(new File("example/src/main/keys/client.keystore"), "client", "password", "password");
        TLSUtil.export(jks, new File("example/src/main/keys"));
        logger.info("done exporting client certificate and key.");
        jks = new JKS(new File("example/src/main/keys/server.keystore"), "server", "password", "password");
        TLSUtil.export(jks, new File("example/src/main/keys"));
        logger.info("done exporting server certificate and key.");
    }

}
