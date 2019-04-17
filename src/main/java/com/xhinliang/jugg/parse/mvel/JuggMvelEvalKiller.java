package com.xhinliang.jugg.parse.mvel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import org.mvel2.MVEL;

import com.xhinliang.jugg.context.CommandContext;
import com.xhinliang.jugg.loader.IBeanLoader;
import com.xhinliang.jugg.parse.IJuggEvalKiller;
import com.xhinliang.jugg.parse.ognl.JuggOgnlContext;

/**
 * @author xhinliang <xhinliang@gmail.com>
 * Created on 2019-04-16
 */
public class JuggMvelEvalKiller implements IJuggEvalKiller {

    private Function<String, JuggOgnlContext> localContextSupplier;

    public JuggMvelEvalKiller(IBeanLoader beanLoader) {
        this.localContextSupplier = new Function<String, JuggOgnlContext>() {

            private ConcurrentMap<String, JuggOgnlContext> contextMap = new ConcurrentHashMap<>();

            @Override
            public JuggOgnlContext apply(String commandContext) {
                return contextMap.computeIfAbsent(commandContext, (key) -> new JuggOgnlContext(beanLoader));
            }
        };
    }

    @Override
    public Object eval(CommandContext commandContext) {
        String command = commandContext.getCommand();
        return MVEL.eval(command, localContextSupplier.apply(commandContext.getJuggUser().getUserName()));
    }
}
