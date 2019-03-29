package com.xhinliang.jugg.plugin.help;

import static com.google.common.collect.Iterables.isEmpty;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.xhinliang.jugg.context.CommandContext;
import com.xhinliang.jugg.handler.IJuggHandler;

/**
 * @author xhinliang <xhinliang@gmail.com>
 * Created on 2019-03-21
 */
public class JuggHelpHandler implements IJuggHandler, JuggHelpable {

    private final Map<Predicate<String>, Function<CommandContext, String>> predicateFunctionMap;

    private final Joiner joiner = Joiner.on("\n");

    private final Map<String, JuggHelpable> helpableMap;

    private final Pattern allHelpPattern = Pattern.compile("^help$");
    private final Pattern helpPattern = Pattern.compile("^help (.*)$");

    public JuggHelpHandler(List<?> handlerList) {
        this.helpableMap = handlerList.stream() //
                .filter(h -> h instanceof JuggHelpable) //
                .collect(toMap(h -> ((JuggHelpable) h).name(), h -> (JuggHelpable) h));

        this.predicateFunctionMap = ImmutableMap //
                .<Predicate<String>, Function<CommandContext, String>> builder() //
                .put(s -> allHelpPattern.matcher(s).find(), this::handleAllHelp) //
                .put(s -> helpPattern.matcher(s).find(), this::handleHelp) //
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

    @SuppressWarnings("unused")
    private String handleAllHelp(CommandContext context) {
        StringBuilder sb = new StringBuilder("help\n");
        helpableMap.forEach((name, helpable) -> {
            sb.append(name).append("\n");
            helpable.patternToMessage()
                    .forEach((pattern, message) -> sb.append(String.format("%-30s  --- %s", pattern, message)).append("\n"));
        });
        return sb.toString();
    }

    private List<String> subCommands(String helpCommand) {
        JuggHelpable handler = helpableMap.get(helpCommand);
        if (handler == null) {
            return emptyList();
        }

        Map<String, String> helpMessages = handler.patternToMessage();
        if (helpMessages == null || helpMessages.isEmpty()) {
            return emptyList();
        }
        return helpMessages.entrySet().stream() //;
                .map(e -> (String.format("%20s  --- %s\n", e.getKey(), e.getValue()))) //
                .collect(Collectors.toList());
    }

    private String handleHelp(CommandContext context) {
        String command = context.getCommand();
        Matcher m = helpPattern.matcher(command);
        if (!m.find()) {
            return null;
        }

        String handlerName = m.group(1);
        List<String> handlerHelpMessages = subCommands(handlerName);
        if (isEmpty(handlerHelpMessages)) {
            return "handler not found!\n";
        }

        String firstLine = "\n";
        return firstLine + joiner.join(handlerHelpMessages);
    }

    @Override
    public String name() {
        return "help";
    }

    @Override
    public Map<String, String> patternToMessage() {
        return ImmutableMap.<String, String> builder() //
                .put("help", "list all help messages.") //
                .put("help {{handlerName}}", "list help messages of this handler") //
                .build();
    }
}
