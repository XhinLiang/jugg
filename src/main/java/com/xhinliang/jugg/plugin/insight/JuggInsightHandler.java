package com.xhinliang.jugg.plugin.insight;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.xhinliang.jugg.context.CommandContext;
import com.xhinliang.jugg.handler.IJuggHandler;
import com.xhinliang.jugg.parse.IJuggEvalKiller;

/**
 * @author xhinliang <xhinliang@gmail.com>
 * Created on 2019-03-24
 */
public class JuggInsightHandler implements IJuggHandler {

    private final JuggInsightService insightService = new JuggInsightServiceImpl();

    private final Joiner joiner = Joiner.on("\n");

    private final IJuggEvalKiller evalKiller;

    private final Map<Predicate<String>, Function<CommandContext, String>> predicateFunctionMap;

    public JuggInsightHandler(IJuggEvalKiller evalKiller) {
        this.evalKiller = evalKiller;
        this.predicateFunctionMap = ImmutableMap //
                .<Predicate<String>, Function<CommandContext, String>> builder() //
                .put(s -> s.matches("^method (.*)$"), this::methods) //
                .put(s -> s.matches("^field (.*)$"), this::fields) //
                .build();
    }

    public void handle(CommandContext context) {
        String result = predicateFunctionMap.entrySet().stream() //
                .filter(e -> e.getKey().test(context.getCommand())) //
                .findFirst() //
                .map(Map.Entry::getValue) //
                .map(f -> f.apply(context)) //
                .orElse(null);

        if (result != null) {
            context.setResult(result);
            context.setShouldEnd(true);
        }
    }

    @Nullable
    public String methods(CommandContext context) {
        String command = context.getCommand();
        String pattern = "^method (.*)$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(command);

        if (!m.find()) {
            return null;
        }

        String targetOgnlCommand = m.group(1);
        Object target = evalKiller.eval(new CommandContext(context.getJuggUser(), targetOgnlCommand));
        return joiner.join(insightService.methods(target));
    }

    @Nullable
    public String fields(CommandContext context) {
        String command = context.getCommand();
        String pattern = "^field (.*)$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(command);

        if (!m.find()) {
            return null;
        }

        String targetOgnlCommand = m.group(1);
        Object target = evalKiller.eval(new CommandContext(context.getJuggUser(), targetOgnlCommand));
        return joiner.join(insightService.fields(target));
    }
}
