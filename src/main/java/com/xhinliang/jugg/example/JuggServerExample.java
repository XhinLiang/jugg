package com.xhinliang.jugg.example;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.xhinliang.jugg.handler.IJuggInterceptor;
import com.xhinliang.jugg.handler.JuggAliasHandler;
import com.xhinliang.jugg.handler.JuggEvalHandler;
import com.xhinliang.jugg.loader.FlexibleBeanLoader;
import com.xhinliang.jugg.loader.IBeanLoader;
import com.xhinliang.jugg.parse.JuggEvalKiller;
import com.xhinliang.jugg.websocket.JuggWebSocketServer;

/**
 * @author xhinliang
 */
public final class JuggServerExample {

    private static final Logger logger = LoggerFactory.getLogger(JuggServerExample.class);
    private static final int DEFAULT_PORT = 10010;

    public static void main(String[] args) throws Exception {
        JuggServerExample example = new JuggServerExample();
        example.startServer();
    }

    private void startServer() throws InterruptedException {
        IBeanLoader beanLoader = new FlexibleBeanLoader() {

            @Nullable
            @Override
            protected Object getActualBean(String name) {
                if (name.equals("testBean")) {
                    return new MockBeanLoader.TestBean();
                }
                return null;
            }

            @Nullable
            @Override
            public Object getBeanByClass(@Nonnull Class<?> clazz) {
                return null;
            }
        };

        JuggEvalKiller evalKiller = new JuggEvalKiller(beanLoader);

        List<IJuggInterceptor> handlers = Lists.newArrayList(//
                context -> logger.info("scope start, command: {}", context.getCommand()),
//                new JuggLoginHandler((username, password) -> password.equals("1l1l1l")), //
                new JuggAliasHandler(beanLoader), //
                new JuggEvalHandler(evalKiller), //
                context -> logger.info("scope end"));

        JuggWebSocketServer webSocketServer = new JuggWebSocketServer(DEFAULT_PORT, handlers);
        webSocketServer.start();
    }
}
