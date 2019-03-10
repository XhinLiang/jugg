package com.xhinliang.jugg.handler;

import com.xhinliang.jugg.context.CommandContext;

/**
 * @author xhinliang
 */
public interface IJuggHandler extends IJuggInterceptor {

    @Override
    default void intercept(CommandContext context) {
        if (context.isShouldEnd()) {
            return;
        }
        handle(context);
    }

    void handle(CommandContext context);
}
