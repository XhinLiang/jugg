package com.xhinliang.jugg.handler;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.xhinliang.jugg.util.FunctionUtils.getJsonLimited;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.xhinliang.jugg.context.CommandContext;
import com.xhinliang.jugg.parse.IJuggEvalKiller;
import com.xhinliang.jugg.plugin.help.JuggHelpable;

/**
 * @author xhinliang
 */
public class JuggEvalHandler implements IJuggHandler, JuggHelpable {

    private IJuggEvalKiller evalKiller;

    public JuggEvalHandler(IJuggEvalKiller evalKiller) {
        this.evalKiller = evalKiller;
    }

    @Override
    public void handle(CommandContext context) {
        String result = firstNonNull(getJsonLimited(evalKiller.eval(context.getCommand(), context.getJuggUser().getUsername())), "null");
        context.setResult(result);
    }

    @Override
    public String name() {
        return "eval";
    }

    @Override
    public Map<String, String> patternToMessage() {
        return ImmutableMap.<String, String> builder() //
                .put("obj", "obj to json, obj could be a OGNL local field or a bean.") //
                .put("bean.method()", "call method of bean") //
                .put("bean.method(1L)", "call method of bean with param 1L") //
                .put("obj = bean.method(1L)", "call method of bean with param 1L, and set the result as a OGNL local field call 'obj'.") //
                .build();
    }
}
