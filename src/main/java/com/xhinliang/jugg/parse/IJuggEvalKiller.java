package com.xhinliang.jugg.parse;

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
}
