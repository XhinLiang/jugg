package com.xhinliang.jugg.plugin.help;

import java.util.Map;

/**
 * @author xhinliang <xhinliang@gmail.com>
 * Created on 2019-03-28
 */
public interface JuggHelpable {

    String name();

    Map<String, String> patternToMessage();
}
