package com.xhinliang.jugg.handler;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.xhinliang.jugg.util.FunctionUtils.getJsonLimited;

import com.xhinliang.jugg.context.CommandContext;
import com.xhinliang.jugg.parse.IJuggEvalKiller;

/**
 * @author xhinliang
 */
public class JuggEvalHandler implements IJuggHandler {

    private IJuggEvalKiller evalKiller;

    public JuggEvalHandler(IJuggEvalKiller evalKiller) {
        this.evalKiller = evalKiller;
    }

    @Override
    public void handle(CommandContext context) {
        String result = firstNonNull(getJsonLimited(evalKiller.eval(context)), "null");
        context.setResult(result);
    }
}
