package com.xhinliang.jugg.parse.mvel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import javax.annotation.Nonnull;

import org.mvel2.MVEL;

import com.xhinliang.jugg.loader.IBeanLoader;
import com.xhinliang.jugg.parse.IJuggEvalKiller;

/**
 * @author xhinliang <xhinliang@gmail.com>
 * Created on 2019-04-16
 */
public class JuggMvelEvalKiller implements IJuggEvalKiller {

    private Function<String, JuggMvelContext> globalContextSupplier;

    private Function<String, HashMap<String, Object>> localContextSupplier;

    public JuggMvelEvalKiller(IBeanLoader beanLoader) {
        this.globalContextSupplier = new Function<String, JuggMvelContext>() {

            private ConcurrentMap<String, JuggMvelContext> contextMap = new ConcurrentHashMap<>();

            @Override
            public JuggMvelContext apply(String commandContext) {
                return contextMap.computeIfAbsent(commandContext, (key) -> new JuggMvelContext(beanLoader));
            }
        };

        this.localContextSupplier = new Function<String, HashMap<String, Object>>() {

            private ConcurrentMap<String, JuggMvelContext> contextMap = new ConcurrentHashMap<>();

            @Override
            public JuggMvelContext apply(String commandContext) {
                return contextMap.computeIfAbsent(commandContext, (key) -> new JuggMvelContext(beanLoader));
            }
        };
    }

    @Override
    public Object eval(String command, String username) {
        // localContext 貌似现在没啥用的
        return MVEL.eval(command, localContextSupplier.apply(username), getContext(username));
    }

    @Nonnull
    @Override
    public Map<String, Object> getContext(String username) {
        return globalContextSupplier.apply(username);
    }
}
