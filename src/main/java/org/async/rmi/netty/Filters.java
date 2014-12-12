package org.async.rmi.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.compression.JdkZlibDecoder;
import io.netty.handler.codec.compression.JdkZlibEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.async.rmi.Factory;
import org.async.rmi.Modules;
import org.async.rmi.config.ID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Barak Bar Orion
 * 12/6/14.
 */
public class Filters {
    private static final Logger logger = LoggerFactory.getLogger(Filters.class);

    private static final int MASK = 0x0001;

    public static int encode(List<String> filters) {
        int res = 0;
        if (filters.contains("drop")) {
            res = res | MASK;
        }
        if (filters.contains("compress")) {
            res = res | (MASK << 1);
        }
        if (filters.contains("encrypt")) {
            res = res | (MASK << 2);
        }
        return res;
    }

    public static boolean hasDrop(int filters) {
        return 0 < (filters & MASK);
    }

    public static boolean hasCompress(int filters) {
        return 0 < (filters & (MASK << 1));
    }

    public static boolean hasEncrypt(int filters) {
        return 0 < (filters & (MASK << 2));
    }

    public static List<String> decode(int filters) {
        List<String> res = new ArrayList<>();
        if (hasDrop(filters)) {
            res.add("drop");
        }
        if (hasCompress(filters)) {
            res.add("compress");
        }
        if (hasEncrypt(filters)) {
            res.add("encrypt");
        }
        return res;
    }

    public static void installFilters(ChannelHandlerContext ctx, int filters, boolean isClient) throws SSLException, CertificateException {
        if (filters != 0) {
            //order backward, since we using addFirst.
            if (hasCompress(filters)) {
                addCompression(ctx);
            }

            // this should be last.
            if (Filters.hasEncrypt(filters)) {
                if (isClient) {
                    addClientEncryption(ctx);
                } else {
                    addServerEncryption(ctx);
                }
            }
        }
    }

    private static void addServerEncryption(ChannelHandlerContext ctx) throws SSLException, CertificateException {
        Channel ch = ctx.pipeline().channel();
        SslContext sslCtx;

/*
        Netmap.TLS tls = Modules.getInstance().getConfiguration().getNetmap().getTls();
        if(tls != null){
            InputStream is = new FileInputStream(tls.getKeystore());
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(is, tls.getKeystorePassword() != null ? tls.getKeystorePassword().toCharArray() : null);
//            KeyManagerFactory kmf
//            Certificate cert = keystore.getCertificate("issuer");
//            sslCtx = SslContext.newServerContext();

            sslCtx = new JdkSslServerContext(servertTrustCrtFile, null,
                    serverCrtFile, serverKeyFile, serverKeyPassword, null,
                    null, IdentityCipherSuiteFilter.INSTANCE, (ApplicationProtocolConfig) null, 0, 0);

        }
*/
        ID id = Modules.getInstance().getConfiguration().getNetMap().getId();
        Factory<SslContext> sslServerContextFactory = Modules.getInstance().getConfiguration().getSslServerContextFactory();
        if (sslServerContextFactory != null) {
            sslCtx = sslServerContextFactory.create();
            logger.debug("using sslServerContextFactory to create ssl context");
        } else if (id != null) {
            sslCtx = SslContext.newServerContext(id.getCertificate(), id.getKey());
            logger.debug("using id to create ssl context");
        } else {
            logger.debug("creating self signed certificate");
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
//            try {
//                String [] file = TLSUtil.JKSToPrivateKey("/home/barakbo/opensource/asyncrmi/example/src/main/keys/server.keystore", "password", "server"
//                        , "password", new File("/home/barakbo/opensource/asyncrmi/example/src/main/keys/"));
//                sslCtx = SslContext.newServerContext(new File(file[0]), new File(file[1]));
//            }catch(Exception e){
//                logger.error(e.toString(), e);
//                SelfSignedCertificate ssc = new SelfSignedCertificate();
//                sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
//            }

    }

    ctx.pipeline().

    addFirst(sslCtx.newHandler(ch.alloc()

    ));
}

    private static void addClientEncryption(ChannelHandlerContext ctx) throws SSLException {
        Factory<SslContext> sslClientContextFactory = Modules.getInstance().getConfiguration().getSslClientContextFactory();
        final SslContext sslCtx = sslClientContextFactory != null ? sslClientContextFactory.create() : SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);
        Channel ch = ctx.pipeline().channel();
        InetSocketAddress address = (InetSocketAddress) ch.remoteAddress();
        ctx.pipeline().addFirst(sslCtx.newHandler(ch.alloc(), address.getHostString(), address.getPort()));
    }

    private static void addCompression(ChannelHandlerContext ctx) {
        ctx.pipeline().addFirst(new JdkZlibEncoder(), new JdkZlibDecoder());
    }


}
