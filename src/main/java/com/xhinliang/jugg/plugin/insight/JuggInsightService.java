package com.xhinliang.jugg.plugin.insight;

import java.util.List;

/**
 * @author xhinliang
 */
public interface JuggInsightService {

    List<String> methods(Object targetObject);

    List<String> fields(Object targetObject);
}
