package com.xhinliang.jugg.plugin.history;

import java.util.List;

/**
 * @author xhinliang <xhinliang@gmail.com>
 * Created on 2019-03-21
 */
public interface JuggHistoryService {

    void addHistory(String username, String command);

    List<String> query(String username, String keyword);
}
