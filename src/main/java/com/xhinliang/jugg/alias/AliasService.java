package com.xhinliang.jugg.alias;

import java.util.Map;

import com.xhinliang.jugg.loader.IBeanLoader;

/**
 * @author ligang03
 */
public interface AliasService {

    void addLocalTargetAlia(String alia, String realName, IBeanLoader beanLoader) throws ClassNotFoundException;

    String getTargetRealNameIfNeed(String rawName);

    Map<String, String> getTargetAlias();
}
