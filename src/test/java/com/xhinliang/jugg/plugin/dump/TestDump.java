package com.xhinliang.jugg.plugin.dump;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.xhinliang.jugg.context.CommandContext;
import com.xhinliang.jugg.context.JuggUser;
import com.xhinliang.jugg.util.JsonMapperUtils;

/**
 * @author xhinliang
 */
public class TestDump {

    @Test
    void test() {
        JuggEvalDumpService service = JuggEvalDumpServiceImpl.getInstance();

        Map<String, Object> context = new HashMap<>();
        context.put("example", new CommandContext(new JuggUser(), "xx"));
        service.save("guest", context);

        List<Long> ids = service.list("guest");
        Map<String, Object> result1 = service.load("guest", ids.get(0));
        Map<String, Object> result2 = service.load("guest", ids.get(0));
        Map<String, Object> result3 = service.load("guest", ids.get(0));

        System.out.println(JsonMapperUtils.toPrettyJson(result1));
        System.out.println(JsonMapperUtils.toPrettyJson(result2));
        System.out.println(JsonMapperUtils.toPrettyJson(result3));
    }
}
