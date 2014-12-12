package org.async.example.ssl.server;

import org.async.example.ssl.Server;
import org.async.rmi.Netmap;
import org.async.rmi.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.rmi.RemoteException;

/**
 * Created by Barak Bar Orion
 * 12/9/14.
 */
public class ServerImpl implements Server {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(ServerImpl.class);


    @Override
    public String echo(String msg) throws RemoteException {
        return msg;
    }


    public static void main(String[] args) throws Exception {
        System.setProperty("java.rmi.server.netmapfile", "ssl.server.netmap.yml");
        Netmap netmap = Netmap.readNetMapFile(new File("ssl.server.netmap.yml"));
        Server server = new ServerImpl();
        Util.writeToFile(server, new File(new File(".."), SER_FILE_NAME));

/*
        InputStream is = new FileInputStream("example/src/main/keys/server.keystore");
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(is, "password".toCharArray());
        Certificate[] chain = keystore.getCertificateChain("server");
        logger.info("chain is of length {}, {}", chain.length, Arrays.asList(chain));
        Key key = keystore.getKey("server", "password".toCharArray());

//            KeyManagerFactory kmf
//            Certificate cert = keystore.getCertificate("issuer");
//            sslCtx = SslContext.newServerContext();


*/
        Thread.sleep(Long.MAX_VALUE);
    }
}
