package com.xhinliang.jugg.websocket;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xhinliang.jugg.handler.IJuggInterceptor;
import com.xhinliang.jugg.util.FunctionUtils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author xhinliang
 */
public class JuggWebSocketServer {

    private static final Logger logger = LoggerFactory.getLogger(JuggWebSocketServer.class);
    private static final int OPTION_SO_BACKLOG = 128;

    private int port;
    private List<IJuggInterceptor> handlers;
    private Function<String, File> fileLoader;

    public JuggWebSocketServer(int port, List<IJuggInterceptor> handlers) {
        this(port, handlers, JuggWebSocketServer::getResourceAsFile);
    }

    public JuggWebSocketServer(int port, List<IJuggInterceptor> handlers, Function<String, File> fileLoader) {
        this.port = port;
        this.handlers = handlers;
        this.fileLoader = fileLoader;
    }

    public void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup) //
                    .channel(NioServerSocketChannel.class) //
                    .childHandler(new WebSocketServerInitializer(handlers, fileLoader)) //
                    .option(ChannelOption.SO_BACKLOG, OPTION_SO_BACKLOG) //
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            logger.info("WebSocket Server started: {}", port);
            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            logger.info("WebSocket Server stop");
        }
    }

    public void startOnNewThread() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                this.start();
            } catch (InterruptedException e) {
                // pass
            }
        });
    }

    private static File getResourceAsFile(String resourcePath) {
        try {
            InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(resourcePath);
            if (in == null) {
                return null;
            }
            return FunctionUtils.getTempFileFromInputStream(in);
        } catch (IOException e) {
            return null;
        }
    }
}
