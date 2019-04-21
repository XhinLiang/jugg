package com.xhinliang.jugg.plugin.dump;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author xhinliang
 */
public interface JuggEvalDumpService {

    long save(String username, Map<String, Object> values);

    void remove(String username, long id);

    Map<String, Object> load(String username, long id);

    List<Long> list(String username);

    boolean dropDb() throws IOException;
}
