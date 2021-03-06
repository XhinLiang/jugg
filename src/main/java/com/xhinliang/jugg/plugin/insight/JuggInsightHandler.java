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

    private final Pattern methodPattern = Pattern.compile("^method (.*)$");
    private final Pattern fieldPattern = Pattern.compile("^field (.*)$");

    private final Joiner joiner = Joiner.on("\n");

    private final IJuggEvalKiller evalKiller;

    private final JuggInsightService insightService;

    private final Map<Predicate<String>, Function<CommandContext, String>> predicateFunctionMap;

    public JuggInsightHandler(IJuggEvalKiller evalKiller) {
        this(null, evalKiller);
    }

    public JuggInsightHandler(@Nullable Function<String, String> fqcnFun, IJuggEvalKiller evalKiller) {
        this.evalKiller = evalKiller;
        this.insightService = new JuggInsightServiceImpl(fqcnFun);
        this.predicateFunctionMap = ImmutableMap //
                .<Predicate<String>, Function<CommandContext, String>> builder() //
                .put(s -> methodPattern.matcher(s).find(), this::methods) //
                .put(s -> fieldPattern.matcher(s).find(), this::fields) //
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
        Matcher m = methodPattern.matcher(command);

        if (!m.find()) {
            return null;
        }

        String targetOgnlCommand = m.group(1);
        Object target = evalKiller.eval(targetOgnlCommand, context.getJuggUser().getUsername());
        String firstLine = String.format("methods of %s\n",
                target instanceof Class ? ((Class) target).getName() : target.getClass().getName());
        return firstLine + joiner.join(insightService.methods(target));
    }

    @Nullable
    public String fields(CommandContext context) {
        String command = context.getCommand();
        Matcher m = fieldPattern.matcher(command);

        if (!m.find()) {
            return null;
        }

        String targetOgnlCommand = m.group(1);
        Object target = evalKiller.eval(targetOgnlCommand, context.getJuggUser().getUsername());
        String firstLine = String.format("fields of %s\n",
                target instanceof Class ? ((Class) target).getName() : target.getClass().getName());
        return firstLine + joiner.join(insightService.fields(target));
    }
}
