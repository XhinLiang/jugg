package com.xhinliang.jugg.parse;

import com.xhinliang.jugg.context.CommandContext;

/**
 * @author xhinliang <xhinliang@gmail.com>
 */
public interface IJuggEvalKiller {

    /**
     * Eval a command and get the value.
     * @param commandContext command & context from client.
     * @return eval result.
     */
    Object eval(CommandContext commandContext);
}
