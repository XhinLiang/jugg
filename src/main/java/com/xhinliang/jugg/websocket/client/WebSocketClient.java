package com.xhinliang.jugg.websocket.client;

import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

/**
 * @author xhinliang
 */
public class WebSocketClient {

    private URI uri;
    private Consumer<String> messageConsumer;

    private Channel ch;
    private EventLoopGroup group = new NioEventLoopGroup();

    public WebSocketClient(String uri, Consumer<String> messageConsumer) {
        this.uri = URI.create(uri);
        this.messageConsumer = messageConsumer;
    }

    public void open() throws Exception {
        Bootstrap b = new Bootstrap();
        String protocol = uri.getScheme();
        if (!"ws".equals(protocol)) {
            throw new IllegalArgumentException("Unsupported protocol: " + protocol);
        }

        // CHECKSTYLE:OFF
        // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
        // If you change it to V00, ping is not supported and remember to change
        // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
        WebSocketClientHandler handler = new WebSocketClientHandler(
                WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, //
                        false, EmptyHttpHeaders.INSTANCE, 1280000), //
                messageConsumer);

        b.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {

            @Override
            public void initChannel(SocketChannel c) throws Exception {
                ChannelPipeline pipeline = c.pipeline();
                pipeline.addLast("http-codec", new HttpClientCodec());
                pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
                pipeline.addLast("ws-handler", handler);
            }
        });
        // CHECKSTYLE:ON

        ch = b.connect(uri.getHost(), uri.getPort()).sync().channel();
        handler.handshakeFuture().sync();
    }

    public void close() throws InterruptedException {
        ch.writeAndFlush(new CloseWebSocketFrame());
        ch.closeFuture().sync();
        //group.shutdownGracefully();
    }

    public void eval(String text) throws IOException {
        ch.writeAndFlush(new TextWebSocketFrame(text));
    }
}
