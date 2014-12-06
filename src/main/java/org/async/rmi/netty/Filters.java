package org.async.rmi.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.compression.JdkZlibDecoder;
import io.netty.handler.codec.compression.JdkZlibEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;

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
                if(isClient) {
                    addClientEncryption(ctx);
                }else{
                    addServerEncryption(ctx);
                }
            }
        }
    }

    private static void addServerEncryption(ChannelHandlerContext ctx) throws CertificateException, SSLException {
        Channel ch = ctx.pipeline().channel();
        InetSocketAddress address = (InetSocketAddress) ch.remoteAddress();
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        final SslContext sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
        ctx.pipeline().addFirst(sslCtx.newHandler(ch.alloc(), address.getHostString(), address.getPort()));
    }

    private static void addClientEncryption(ChannelHandlerContext ctx) throws SSLException {
        Channel ch = ctx.pipeline().channel();
        InetSocketAddress address = (InetSocketAddress) ch.remoteAddress();
        SslContext sslCtx = SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);
        ctx.pipeline().addFirst(sslCtx.newHandler(ch.alloc(), address.getHostString(), address.getPort()));
    }

    private static void addCompression(ChannelHandlerContext ctx) {
        ctx.pipeline().addFirst(new JdkZlibEncoder(), new JdkZlibDecoder());
    }

}
