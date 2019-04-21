package com.xhinliang.jugg.plugin.dump;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.xhinliang.jugg.context.CommandContext;
import com.xhinliang.jugg.context.JuggUser;
import com.xhinliang.jugg.util.JsonMapperUtils;

/**
 * @author xhinliang
 */
public class TestDump {

    @Test
    void test() throws IOException {
        JuggEvalDumpService service = JuggEvalDumpServiceImpl.getInstance();

        Map<String, Object> context = new HashMap<>();
        context.put("example", new CommandContext(new JuggUser(), "xx"));
        service.save("guest", context);

        List<Long> ids = service.list("guest");
        Map<String, Object> result1 = service.load("guest", ids.get(0));
        Map<String, Object> result2 = service.load("guest", ids.get(0));
        Map<String, Object> result3 = service.load("guest", ids.get(0));

        CommandContext context1 = (CommandContext) result1.get("example");
        Assertions.assertEquals("xx", context1.getCommand());
        CommandContext context2 = (CommandContext) result2.get("example");
        Assertions.assertEquals("xx", context2.getCommand());
        CommandContext context3 = (CommandContext) result3.get("example");
        Assertions.assertEquals("xx", context3.getCommand());

        System.out.println(JsonMapperUtils.toPrettyJson(result1));
        System.out.println(JsonMapperUtils.toPrettyJson(result2));
        System.out.println(JsonMapperUtils.toPrettyJson(result3));

        service.dropDb();
    }
}
