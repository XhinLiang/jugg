package com.xhinliang.jugg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class TestWebSocket {

    /**
     * 测试多 Client 并发的情况下是否存在问题
     */
    @Test
    @Disabled
    void test() throws Exception {
        startServer();

//        for (int i = 1; i < 40; i++) {
//            String username = "xhinliang" + i;
//            new Thread(() -> startClientAndSendEval(username)).start();
//        }

        String evalStr = "abcd";
        WebSocketClient client = new WebSocketClient("ws://localhost:10010/ws", text -> Assertions.assertEquals("\"+ " + evalStr + " +\"", text));

        try {
            client.open();
            client.eval("login xhinliang 1l1l1l");
            client.eval("@JsonMapperUtils@toPrettyJson(\"abcd\")");
        } catch (Exception e) {
            Assertions.fail("", e);
        }
        Thread.sleep(3000L);
    }

    private void startServer() throws InterruptedException {
        Thread serverThread = new Thread(() -> {
            try {
                JuggServerExample.main(new String[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        serverThread.start();
        Thread.sleep(5000L);
    }

    private void startClientAndSendEval(String username) {
        WebSocketClient client = new WebSocketClient("ws://localhost:10010/ws", text -> {
            // 注意这里并不是发起 Client 的线程
            if (!text.contains("system")) {
                System.out.println("receive: " + text);
                Assertions.assertEquals('"' + username + '"', text);
            }
        });
        try {
            client.open();
            client.eval("login " + username + " 1l1l1l");
            client.eval("#testBean.ping(\"" + username + "\")");
        } catch (Exception e) {
            Assertions.fail("", e);
        }
    }
}