package com.xhinliang.jugg;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.xhinliang.jugg.handler.IJuggInterceptor;
import com.xhinliang.jugg.handler.JuggEvalHandler;
import com.xhinliang.jugg.parse.mvel.JuggConfiglessMvelEvalKiller;
import com.xhinliang.jugg.plugin.help.JuggHelpHandler;
import com.xhinliang.jugg.plugin.history.JuggHistoryHandler;
import com.xhinliang.jugg.plugin.insight.JuggInsightHandler;
import com.xhinliang.jugg.plugin.preload.JuggPreloadHandler;
import com.xhinliang.jugg.websocket.JuggWebSocketServer;

/**
 * @author xhinliang
 */
public final class JuggConfiglessServer {

    private static final Logger logger = LoggerFactory.getLogger(JuggConfiglessServer.class);
    private static final int DEFAULT_PORT = 10010;

    public static void main(String[] args) throws Exception {
        JuggConfiglessServer example = new JuggConfiglessServer();
        example.startServer();
    }

    private void startServer() throws InterruptedException {
        JuggConfiglessMvelEvalKiller evalKiller = new JuggConfiglessMvelEvalKiller();

        List<IJuggInterceptor> handlers = Lists.newArrayList(//
                context -> logger.info("scope start, command: {}", context.getCommand()), //
                new JuggInsightHandler(null, evalKiller), //
                new JuggHistoryHandler(evalKiller), //
                new JuggPreloadHandler(evalKiller, ImmutableList.of()),
                new JuggEvalHandler(evalKiller), //
                context -> logger.info("scope end"));

        handlers.add(0, new JuggHelpHandler(handlers));

        JuggWebSocketServer webSocketServer = new JuggWebSocketServer(DEFAULT_PORT, handlers);
        webSocketServer.start();
    }
}
