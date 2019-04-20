package com.xhinliang.jugg.parse.mvel;

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

    private Function<String, JuggMvelContext> contextSupplier;

    public JuggMvelEvalKiller(IBeanLoader beanLoader) {
        this.contextSupplier = new Function<String, JuggMvelContext>() {

            private ConcurrentMap<String, JuggMvelContext> contextMap = new ConcurrentHashMap<>();

            @Override
            public JuggMvelContext apply(String commandContext) {
                return contextMap.computeIfAbsent(commandContext, (key) -> new JuggMvelContext(beanLoader));
            }
        };
    }

    @Override
    public Object eval(CommandContext commandContext) {
        return eval(commandContext.getCommand(), commandContext.getJuggUser().getUserName());
    }

    @Override
    public Object eval(String command, String username) {
        JuggMvelContext varsContext = contextSupplier.apply(username);
        return MVEL.eval(command, varsContext, varsContext);
    }
}
