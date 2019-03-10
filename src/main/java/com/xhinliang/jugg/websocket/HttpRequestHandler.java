package com.xhinliang.jugg.websocket;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedNioFile;

/**
 * @author xhinliang
 */
class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequestHandler.class);
    private final String wsUri;
    private Function<String, File> fileLoader;

    HttpRequestHandler(String wsUri, Function<String, File>  fileLoader) {
        this.wsUri = wsUri;
        this.fileLoader = fileLoader;
    }

    @Nullable
    private File getFile(String requestPath) {
        String path = "fire_public" + requestPath;
        return fileLoader.apply(path);
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
        ctx.writeAndFlush(response);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        // WebSocket Proto Upgrade.
        if (wsUri.equalsIgnoreCase(request.uri())) {
            ctx.fireChannelRead(request.retain());
            return;
        }

        if (HttpUtil.is100ContinueExpected(request)) {
            send100Continue(ctx);
        }

        String uri = request.uri();
        if (StringUtils.isBlank(uri) || uri.equals("/")) {
            uri = "/index.html";
        }

        File rawFile = getFile(uri);
        if (rawFile == null) {
            HttpResponse response = new DefaultHttpResponse(request.protocolVersion(), HttpResponseStatus.NOT_FOUND);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "*/*; charset=UTF-8");
            logger.error("{} not found!", uri);
            ctx.writeAndFlush(response);
            ctx.close();
            return;
        }

        RandomAccessFile file = new RandomAccessFile(rawFile, "r");

        HttpResponse response = new DefaultHttpResponse(request.protocolVersion(), HttpResponseStatus.OK);
        if (uri.endsWith("html")) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        } else if (uri.endsWith("js")) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/javascript; charset=UTF-8");
        } else if (uri.endsWith("css")) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/css; charset=UTF-8");
        } else {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        }

        boolean keepAlive = HttpUtil.isKeepAlive(request);

        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, file.length());
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        ctx.write(response);
        if (ctx.pipeline().get(SslHandler.class) == null) {
            ctx.write(new DefaultFileRegion(file.getChannel(), 0, rawFile.length()));
        } else {
            ctx.write(new ChunkedNioFile(file.getChannel()));
        }
        ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
        ctx.flush();
        file.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel incoming = ctx.channel();
        logger.error("Client: {}", incoming.remoteAddress(), cause);
        // 当出现异常就关闭连接
        ctx.close();
    }
}
