package com.xhinliang.jugg.plugin.preload;

import java.util.List;

/**
 * @author xhinliang
 */
public interface IJuggPreloader {

    List<String> getScripts();

    String packageName();

    String desc();

    String sampleInput();

    Object sampleOutput();
}
