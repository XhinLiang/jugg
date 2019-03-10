package com.xhinliang.jugg.websocket;

import java.io.File;
import java.util.List;
import java.util.function.Function;

import com.xhinliang.jugg.handler.IJuggInterceptor;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author xhinliang
 */
class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {

    private static final int HTTP_MAX_CONTENT_LENGTH = 64 * 1024 * 1024;
    private List<IJuggInterceptor> handlers;
    private Function<String, File> fileLoader;

    WebSocketServerInitializer(List<IJuggInterceptor> handlers, Function<String, File>  fileLoader) {
        this.handlers = handlers;
        this.fileLoader = fileLoader;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(HTTP_MAX_CONTENT_LENGTH));
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new HttpRequestHandler("/ws", fileLoader));
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
        pipeline.addLast(new TextWebSocketFrameHandler(handlers));
    }
}
