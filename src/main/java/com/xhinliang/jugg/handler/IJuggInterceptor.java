package com.xhinliang.jugg.handler;

import com.xhinliang.jugg.context.CommandContext;

/**
 * Interceptor: 允许拦截请求
 *
 * @author xhinliang
 */
public interface IJuggInterceptor {

    void intercept(CommandContext context);
}
