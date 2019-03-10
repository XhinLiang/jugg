package com.xhinliang.jugg.parse;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import com.xhinliang.jugg.context.CommandContext;
import com.xhinliang.jugg.loader.IBeanLoader;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

/**
 * @author xhinliang
 */
public class JuggEvalKiller implements IJuggEvalKiller {

    private OgnlContext globalContext;

    private Function<String, JuggOgnlContext> localContextSupplier;

    public JuggEvalKiller(IBeanLoader beanLoader) {
        this.globalContext = new JuggOgnlContext(beanLoader);
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
        Map localContext = localContextSupplier.apply(commandContext.getJuggUser().getUserName());
        try {
            return Ognl.getValue(commandContext.getCommand(), globalContext, localContext);
        } catch (OgnlException e) {
            throw new RuntimeException(firstNonNull(e.getReason(), e));
        }
    }
}
