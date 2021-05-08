package org.async.rmi.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.async.rmi.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by Barak Bar Orion
 * 11/14/14.
 */
public class ClassLoaderHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(ClassLoaderHandler.class);
    private final ClassLoader cl;

    public ClassLoaderHandler(ClassLoader cl) {
        this.cl = cl;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (!request.decoderResult().isSuccess()) {
            sendError(ctx, BAD_REQUEST);
            return;
        }
        if (request.method() != GET) {
            sendError(ctx, METHOD_NOT_ALLOWED);
            return;
        }

        final String uri = request.uri();
        final String className = sanitizeUri(uri);
        if (className == null) {
            sendError(ctx, FORBIDDEN);
            return;
        }
        logger.debug("serving http request for class {}", className);
        try {
            byte[] bytes = getClassBytes(className);

            boolean keepAlive = HttpUtil.isKeepAlive(request);
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(bytes));
            response.headers().set(CONTENT_TYPE, "application/x-java-class");
            response.headers().set(CONTENT_LENGTH, String.valueOf(response.content().readableBytes()));
            if (!keepAlive) {
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            } else {
                response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                ctx.writeAndFlush(response);
            }
        } catch (Exception e) {
            logger.warn("exception while serving request {}, for class {}", request, className, e);
            sendError(ctx, INTERNAL_SERVER_ERROR);
        }


    }


    private byte[] getClassBytes(String className) throws IOException {
        String classAsPath = className.replace('.', '/') + ".class";
        logger.debug("finding {} bytes using {}", classAsPath, cl);
        try (InputStream in = cl.getResourceAsStream(classAsPath)) {
            if (in == null) {
                throw new IOException("class " + className + " not found");
            }
            return Util.asByteArray(in);
        }
    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static String sanitizeUri(String uri) {
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }

        if (!uri.startsWith("/")) {
            return null;
        }
        if (uri.endsWith(".class")) {
            uri = uri.replaceFirst("\\.class$", "");
        }
        return uri.substring(1).replaceAll("/", ".");
    }

}
