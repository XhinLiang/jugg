package com.xhinliang.jugg.parse;

import java.util.Map;

import javax.annotation.Nonnull;

/**
 * @author xhinliang <xhinliang@gmail.com>
 */
public interface IJuggEvalKiller {

    /**
     * @param command command
     * @param username username
     * @return eval result.
     */
    Object eval(String command, String username);

    @Nonnull
    Map getContext(String username);
}
