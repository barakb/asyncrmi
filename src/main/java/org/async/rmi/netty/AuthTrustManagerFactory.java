package org.async.rmi.netty;

import io.netty.handler.ssl.util.SimpleTrustManagerFactory;
import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * Created by Barak Bar Orion
 * 12/13/14.
 */
public class AuthTrustManagerFactory extends SimpleTrustManagerFactory {
    private static final Logger logger = LoggerFactory.getLogger(AuthTrustManagerFactory.class);
    private final List<String> auth;

    public AuthTrustManagerFactory(List<String> auth) {
        this.auth = auth;
        //todo ready the certificate of each one in the auth
    }


    private static final TrustManager tm = new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
            logger.debug("Accepting a client certificate: " + chain[0].getSubjectDN());
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
            logger.debug("Accepting a server certificate: " + chain[0].getSubjectDN());
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return EmptyArrays.EMPTY_X509_CERTIFICATES;
        }
    };

    @Override
    protected void engineInit(KeyStore keyStore) throws Exception { }

    @Override
    protected void engineInit(ManagerFactoryParameters managerFactoryParameters) throws Exception { }

    @Override
    protected TrustManager[] engineGetTrustManagers() {
        return new TrustManager[] { tm };
    }

}
