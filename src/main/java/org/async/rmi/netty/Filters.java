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
        ID id = Modules.getInstance().getConfiguration().getNetMap().getId();
        Factory<SslContext> sslServerContextFactory = Modules.getInstance().getConfiguration().getSslServerContextFactory();
        if (sslServerContextFactory != null) {
            sslCtx = sslServerContextFactory.create();
            logger.debug("server using configured sslServerContextFactory to create ssl context");
        } else if (id != null) {
            sslCtx = SslContext.newServerContext(id.getCertificate(), id.getKey());
            logger.debug("server using certificate {} from configured id to create ssl context", id.getCertificate().getAbsolutePath());
        } else {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
            logger.debug("server creating self signed certificate to create ssl context");
    }

    ctx.pipeline().
    addFirst(sslCtx.newHandler(ch.alloc()

    ));
}

    private static void addClientEncryption(ChannelHandlerContext ctx) throws SSLException {
        SslContext sslCtx;
        ID id = Modules.getInstance().getConfiguration().getNetMap().getId();

        Factory<SslContext> sslClientContextFactory = Modules.getInstance().getConfiguration().getSslClientContextFactory();
        if(sslClientContextFactory != null){
            sslCtx = sslClientContextFactory.create();
            logger.debug("client using configured sslClientContextFactory to create ssl context");
        }else if(id != null){
            sslCtx = SslContext.newClientContext(id.getCertificate(), InsecureTrustManagerFactory.INSTANCE);
            logger.debug("client using certificate {} from configured id to create ssl context", id.getCertificate().getAbsolutePath());
        }else{
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
