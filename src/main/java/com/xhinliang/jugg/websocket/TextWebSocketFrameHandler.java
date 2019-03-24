package com.xhinliang.jugg.websocket;

import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static com.xhinliang.jugg.util.FunctionUtils.exceptionToString;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xhinliang.jugg.context.CommandContext;
import com.xhinliang.jugg.context.JuggUser;
import com.xhinliang.jugg.handler.IJuggInterceptor;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * @author xhinliang
 */
class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(TextWebSocketFrameHandler.class);

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private ConcurrentHashMap<Channel, JuggUser> userMap = new ConcurrentHashMap<>();

    // CHECKSTYLE:OFF
    private final ExecutorService executor = Executors.newFixedThreadPool(100);

    private List<IJuggInterceptor> handlers;

    TextWebSocketFrameHandler(List<IJuggInterceptor> handlers) {
        this.handlers = handlers;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, TextWebSocketFrame msg) {
        Channel incoming = context.channel();
        String command = msg.text();
        if (StringUtils.isBlank(command)) {
            // do nothing.
            return;
        }
        asyncHandle(incoming, command);
    }

    private void asyncHandle(Channel incoming, String command) {
        executor.submit(() -> {
            try {
                TextWebSocketFrame frame = handle(incoming, command);
                incoming.writeAndFlush(frame);
            } catch (Exception ex) {
                incoming.writeAndFlush(new TextWebSocketFrame(ex.getMessage()));
            }
        });
    }

    private TextWebSocketFrame handle(Channel incoming, String command) {
        JuggUser juggUser = userMap.get(incoming);
        CommandContext context = new CommandContext(juggUser, command);
        for (IJuggInterceptor interceptor : this.handlers) {
            try {
                interceptor.intercept(context);
            } catch (Exception e) {
                context.setResult(exceptionToString(e));
                context.setShouldEnd(true);
            }
        }
        if (StringUtils.isBlank(context.getResult())) {
            context.setResult("handler not found, check you config");
        }
        return new TextWebSocketFrame(context.getResult());
    }

    @Override
    public void handlerAdded(ChannelHandlerContext context) {
        Channel incoming = context.channel();
        channelGroup.add(incoming);
        executor.submit(() -> {
            sleepUninterruptibly(20, TimeUnit.MILLISECONDS);
            incoming.writeAndFlush(new TextWebSocketFrame("[system] welcome to jugg."));
        });
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext context) {
        // A closed Channel is automatically removed from ChannelGroup,
        // so there is no need to do "channelGroup.remove(context.channel());"
    }

    @Override
    public void channelActive(ChannelHandlerContext context) {
        Channel incoming = context.channel();
        userMap.put(incoming, new JuggUser());
        incoming.writeAndFlush(new TextWebSocketFrame("[system] active."));
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) {
        Channel removed = context.channel();
        JuggUser juggUser = userMap.remove(removed);
        logger.info("juggUser:{} inactive", juggUser.getUserName());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        context.close();
    }

}
