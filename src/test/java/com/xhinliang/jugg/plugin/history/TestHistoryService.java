package com.xhinliang.jugg.plugin.history;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author xhinliang <xhinliang@gmail.com>
 * Created on 2019-03-22
 */
public class TestHistoryService {

    @Test
    void test() {
        JuggHistoryService service = JuggHistoryServiceImpl.getInstance();
        service.addHistory("xhinliang", "abcdfewfew");
        service.addHistory("xhinliang", "abcd.eval()");
        service.addHistory("xhinliang", "abcd.eval2()");

        List<String> history = service.query("xhinliang", "eva");
        Assertions.assertEquals("abcd.eval2()", history.get(0));
        Assertions.assertEquals("abcd.eval()", history.get(1));
        history.forEach(h -> Assertions.assertTrue(h.contains("eva")));

        List<String> allHistory = service.query("xhinliang", null);

        Assertions.assertTrue(allHistory.stream().anyMatch(h -> h.equals("abcdfewfew")));

        List<String> emptyHistory = service.query("abcdXxx", "eva");
        Assertions.assertTrue(emptyHistory.isEmpty());
    }
}
