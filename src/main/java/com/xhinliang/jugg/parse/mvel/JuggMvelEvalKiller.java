package com.xhinliang.jugg.parse.mvel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import org.mvel2.MVEL;

import com.xhinliang.jugg.context.CommandContext;
import com.xhinliang.jugg.loader.IBeanLoader;
import com.xhinliang.jugg.parse.IJuggEvalKiller;

/**
 * @author xhinliang <xhinliang@gmail.com>
 * Created on 2019-04-16
 */
public class JuggMvelEvalKiller implements IJuggEvalKiller {

    private JuggMvelContext globalContext;

    private Function<String, HashMap<String, Object>> localContextSupplier;

    public JuggMvelEvalKiller(IBeanLoader beanLoader) {
        this.globalContext = new JuggMvelContext(beanLoader);
        this.localContextSupplier = new Function<String, HashMap<String, Object>>() {

            private ConcurrentMap<String, HashMap<String, Object>> contextMap = new ConcurrentHashMap<>();

            @Override
            public HashMap<String, Object> apply(String commandContext) {
                return contextMap.computeIfAbsent(commandContext, (key) -> new HashMap<>());
            }
        };
    }

    @Override
    public Object eval(CommandContext commandContext) {
        String command = commandContext.getCommand();
        Map map = localContextSupplier.apply(commandContext.getJuggUser().getUserName());
        return MVEL.eval(command, map, globalContext);
    }
}
