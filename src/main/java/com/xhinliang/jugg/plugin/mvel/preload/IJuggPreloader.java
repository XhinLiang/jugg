package com.xhinliang.jugg.plugin.mvel.preload;

import java.util.List;

/**
 * @author xhinliang
 */
public interface IJuggPreloader {

    List<String> getScripts();

    String packageName();

    String desc();
}
