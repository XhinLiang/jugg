package com.xhinliang.jugg;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.xhinliang.jugg.handler.IJuggInterceptor;
import com.xhinliang.jugg.handler.JuggEvalHandler;
import com.xhinliang.jugg.loader.FlexibleBeanLoader;
import com.xhinliang.jugg.parse.ognl.JuggOgnlEvalKiller;
import com.xhinliang.jugg.plugin.alias.JuggAliasHandler;
import com.xhinliang.jugg.plugin.help.JuggHelpHandler;
import com.xhinliang.jugg.plugin.history.JuggHistoryHandler;
import com.xhinliang.jugg.plugin.insight.JuggInsightHandler;
import com.xhinliang.jugg.plugin.mvel.preload.JuggPreloadHandler;
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
        FlexibleBeanLoader beanLoader = new FlexibleBeanLoader() {

            @Nullable
            @Override
            protected Object getActualBean(String name) {
                if (name.equals("testBean")) {
                    return new TestBean();
                }
                return null;
            }

            @Nullable
            @Override
            public Object getBeanByClass(@Nonnull Class<?> clazz) {
                return null;
            }
        };

        JuggOgnlEvalKiller evalKiller = new JuggOgnlEvalKiller(beanLoader);

        List<IJuggInterceptor> handlers = Lists.newArrayList(//
                context -> logger.info("scope start, command: {}", context.getCommand()), new JuggAliasHandler(beanLoader), //
                new JuggInsightHandler(beanLoader::getFqcnBySimpleClassName, evalKiller), //
                new JuggHistoryHandler(evalKiller), //
                new JuggPreloadHandler(evalKiller, ImmutableList.of()),
                new JuggEvalHandler(evalKiller), //
                context -> logger.info("scope end"));

        handlers.add(0, new JuggHelpHandler(handlers));

        JuggWebSocketServer webSocketServer = new JuggWebSocketServer(DEFAULT_PORT, handlers);
        webSocketServer.start();
    }

    /**
     * just for mock
     */
    static class MockBeanLoader extends FlexibleBeanLoader {

        private TestBean testBean = new TestBean();

        @Override
        public Object getBeanByClass(@Nonnull Class<?> clazz) {
            return clazz == TestBean.class ? testBean : null;
        }

        @Nullable
        @Override
        protected Object getActualBean(String name) {
            if (StringUtils.equals("testBean", name)) {
                return testBean;
            }
            return null;
        }
    }

    /**
     * for test
     */
    @SuppressWarnings({ "unused", "WeakerAccess" })
    private static class TestBean {

        private String value = "xxx";

        private String getVal() {
            return value;
        }

        public long ping(long val) {
            return val;
        }

        public <T> T ping(T val) {
            return val;
        }

        public String add(String a, String b) {
            return a + b;
        }

        public TestBean append(String raw) {
            value += raw;
            return self();
        }

        public TestBean self() {
            return this;
        }
    }
}
