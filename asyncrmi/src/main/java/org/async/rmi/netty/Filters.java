package org.async.rmi.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.compression.JdkZlibDecoder;
import io.netty.handler.codec.compression.JdkZlibEncoder;
import io.netty.handler.ssl.IdentityCipherSuiteFilter;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.async.rmi.Modules;
import org.async.rmi.config.ID;
import org.async.rmi.config.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import java.io.File;
import java.net.InetSocketAddress;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Barak Bar Orion
 * 12/6/14.
 */
@SuppressWarnings("WeakerAccess")
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


    public static void installServerFilters(ChannelHandlerContext ctx, int filters, Rule rule) throws SSLException, CertificateException {
        if (filters != 0) {
            //order backward, since we using addFirst.
            installServerFilters(ctx, filters);

            // this should be last.
            if (Filters.hasEncrypt(filters)) {
                addServerEncryption(ctx, rule);
            }
        }
    }

    public static void installClientFilters(ChannelHandlerContext ctx, int filters) throws SSLException, CertificateException {
        if (filters != 0) {
            //order backward, since we using addFirst.
            installServerFilters(ctx, filters);

            // this should be last.
            if (Filters.hasEncrypt(filters)) {
                addClientEncryption(ctx);
            }
        }
    }

    private static void installServerFilters(ChannelHandlerContext ctx, int filters) throws SSLException, CertificateException {
        if (hasCompress(filters)) {
            addCompression(ctx);
        }
    }

    private static void addServerEncryption(ChannelHandlerContext ctx, Rule rule) throws SSLException, CertificateException {
//        TrustManagerFactory trustManagerFactory = getRuleTrustManager(rule);
        Channel ch = ctx.pipeline().channel();
        SslContext sslCtx;
        ID id = Modules.getInstance().getConfiguration().getNetMap().getId();
        if (id != null) {
            if (rule.getAuth() != null) {
                logger.debug("server using certificate {} from configured id to create ssl context and require client auth: {}", id.getCertificate().getAbsolutePath(), rule.getAuth());
                sslCtx = SslContext.newServerContext(null, new File(rule.getAuth()), null,
                        id.getCertificate(), id.getKey(), null, null, null, IdentityCipherSuiteFilter.INSTANCE, null, 0, 0);
                SSLEngine engine = sslCtx.newEngine(ch.alloc());
                engine.setUseClientMode(false);
                engine.setNeedClientAuth(true);
            } else {
                sslCtx = SslContext.newServerContext(id.getCertificate(), id.getKey());
                logger.debug("server using certificate {} from configured id to create ssl context", id.getCertificate().getAbsolutePath());
            }
            ctx.pipeline().addFirst(sslCtx.newHandler(ch.alloc()));
        } else {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            if (rule.getAuth() != null) {
                logger.debug("server using self signed certificate to create ssl context and require client auth: {}", rule.getAuth());
                sslCtx = SslContext.newServerContext(null, new File(rule.getAuth()), null,
                        ssc.certificate(), ssc.privateKey(), null, null, null, IdentityCipherSuiteFilter.INSTANCE, null, 0, 0);
                SSLEngine engine = sslCtx.newEngine(ch.alloc());
                engine.setUseClientMode(false);
                engine.setNeedClientAuth(true);
            } else {
                logger.debug("server creating self signed certificate to create ssl context");
                sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
            }
            ctx.pipeline().addFirst(sslCtx.newHandler(ch.alloc()));
        }
    }

    private static void addClientEncryption(ChannelHandlerContext ctx) throws SSLException {
        SslContext sslCtx;
        ID id = Modules.getInstance().getConfiguration().getNetMap().getId();

        if (id != null) {
            sslCtx = SslContext.newClientContext(id.getCertificate(), InsecureTrustManagerFactory.INSTANCE);
            logger.debug("client using certificate {} from configured id to create ssl context", id.getCertificate().getAbsolutePath());
        } else {
            sslCtx = SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);
            logger.debug("client creating self signed certificate to create ssl context");
        }

        Channel ch = ctx.pipeline().channel();
        InetSocketAddress address = (InetSocketAddress) ch.remoteAddress();
        ctx.pipeline().addFirst(sslCtx.newHandler(ch.alloc(), address.getHostString(), address.getPort()));
    }

    private static void addCompression(ChannelHandlerContext ctx) {
        ctx.pipeline().addFirst(new JdkZlibEncoder(), new JdkZlibDecoder());
    }


}
