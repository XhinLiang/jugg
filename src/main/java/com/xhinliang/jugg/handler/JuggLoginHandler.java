package com.xhinliang.jugg.handler;

import java.util.function.BiPredicate;

import com.google.common.base.Preconditions;
import com.xhinliang.jugg.context.CommandContext;
import com.xhinliang.jugg.exception.JuggRuntimeException;

/**
 * @author xhinliang
 */
public class JuggLoginHandler implements IJuggHandler {

    private BiPredicate<String, String> passwordChecker;

    public JuggLoginHandler(BiPredicate<String, String> passwordChecker) {
        this.passwordChecker = passwordChecker;
    }

    @Override
    public void handle(CommandContext context) {
        // 已登录直接放行
        if (context.getJuggUser().isLogin()) {
            return;
        }
        // 尝试登陆，走登陆逻辑
        if (context.getCommand().startsWith("login ")) {
            tryLogin(context);
            return;
        }
        // 未登录且不是执行登录操作，则拦截掉
        String result = "[system] you should login first.";
        context.setShouldEnd(true);
        context.setResult(result);
    }

    private void tryLogin(CommandContext context) {
        String[] spliced = context.getCommand().split(" ");
        if (spliced.length != 3) {
            throw new JuggRuntimeException("[system] login syntax error!");
        }
        String userName = spliced[1];
        String password = spliced[2];
        Preconditions.checkNotNull(userName, password);
        if (passwordChecker.test(userName, password)) {
            context.getJuggUser().setLogin(true);
            context.getJuggUser().setUserName(userName);
            String result = "[system] login success.";
            context.setResult(result);
            context.setShouldEnd(true);
        } else {
            String result = "[system] login failed, username and password not match!";
            context.setResult(result);
            context.setShouldEnd(true);
        }
    }
}
