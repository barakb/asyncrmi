package org.async.rmi.tls;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;

/**
 * Created by Barak Bar Orion
 * 12/12/14.
 */
public class TLSUtil {
    private static final Logger logger = LoggerFactory.getLogger(TLSUtil.class);

    /**
     * Export JKS file to private key file and pem certificate chain file.
     * @throws IOException
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     */
    public static File[] export(JKS jks, File to) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        InputStream is = new FileInputStream(jks.getJksFile());
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(is, jks.getFilePassword() != null ? jks.getFilePassword().toCharArray() : null);
        Certificate[] chain = keystore.getCertificateChain(jks.getAlias());
        Key key = keystore.getKey(jks.getAlias(), jks.getAliasPassword() != null ? jks.getAliasPassword().toCharArray() : null);
        return writeKeyAndCertificate(to, jks.getAlias(), key, chain);
    }

    static File[] writeKeyAndCertificate(File directory, String name, Key key, Certificate[] certificates) throws IOException, CertificateEncodingException {

        // Encode the private key into a file.
        String keyText = "-----BEGIN PRIVATE KEY-----\n" + Base64.encode(Unpooled.wrappedBuffer(key.getEncoded()), true).toString(CharsetUtil.US_ASCII) +
                "\n-----END PRIVATE KEY-----\n";

        File keyFile = new File(directory, name + "-private.pem");
        try (OutputStream keyOut = new FileOutputStream(keyFile)) {
            keyOut.write(keyText.getBytes(CharsetUtil.US_ASCII));
        }

        StringBuilder certText = new StringBuilder();
        for (Certificate certificate : certificates) {
            // Encode the certificate into a CRT file.
            certText.append("-----BEGIN CERTIFICATE-----\n")
                    .append(Base64.encode(Unpooled.wrappedBuffer(certificate.getEncoded()), true).toString(CharsetUtil.US_ASCII))
                    .append("\n-----END CERTIFICATE-----\n");
        }

        File certFile = new File(directory, name + "-certificate.pem");

        try (OutputStream certOut = new FileOutputStream(certFile)) {
            certOut.write(certText.toString().getBytes(CharsetUtil.US_ASCII));
        }

        return new File[]{certFile, keyFile};
    }

    public static void main(String[] args) throws Exception{
        JKS jks = new JKS(new File("example/src/main/keys/client.keystore"), "client", "password", "password");
        export(jks, new File("example/src/main/keys"));
        jks = new JKS(new File("example/src/main/keys/server.keystore"), "server", "password", "password");
        export(jks, new File("example/src/main/keys"));
        logger.info("done.");
    }

}
