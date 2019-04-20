package com.xhinliang.jugg.plugin.preload;

import static com.google.common.collect.Iterables.isEmpty;
import static java.util.Collections.emptyList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.xhinliang.jugg.context.CommandContext;
import com.xhinliang.jugg.exception.JuggRuntimeException;
import com.xhinliang.jugg.handler.IJuggHandler;
import com.xhinliang.jugg.parse.IJuggEvalKiller;
import com.xhinliang.jugg.plugin.help.JuggHelpable;

/**
 * @author xhinliang <xhinliang@gmail.com>
 * Created on 2019-03-21
 */
public class JuggPreloadHandler implements IJuggHandler, JuggHelpable {

    private final IJuggEvalKiller evalKiller;

    private final Map<String, IJuggPreloader> preloaderList;

    // map { username -> map { packageName -> isLoaded } }
    private final Map<String, Map<String, Boolean>> preloadedMap;

    public JuggPreloadHandler(IJuggEvalKiller evalKiller) {
        this(evalKiller, ImmutableList.<IJuggPreloader> builder() //
                .add(new CollectionFunctionPreloader()) //
                .build() //
        );
    }

    public JuggPreloadHandler(IJuggEvalKiller evalKiller, List<IJuggPreloader> preloaderList) {
        this.evalKiller = evalKiller;
        this.preloaderList = preloaderList.stream() //
                .collect(toMap(IJuggPreloader::packageName, identity()));
        this.preloadedMap = new ConcurrentHashMap<>();
    }

    public void handle(CommandContext context) {
        String result = this.generateResult(context);
        if (result != null) {
            context.setResult(result);
            context.setShouldEnd(true);
        }
    }

    @Nullable
    public String generateResult(CommandContext context) {
        String command = context.getCommand();

        if (command.equals("preload list")) {
            return list(context.getJuggUser().getUsername());
        }

        if (command.startsWith("preload ")) {
            String[] spliced = command.split(" ");
            if (spliced.length == 2) {
                return preload(context.getJuggUser().getUsername(), spliced[1]);
            } else {
                throw new JuggRuntimeException("[system] preload syntax error!");
            }
        }

        return null;
    }

    private String list(String username) {
        StringBuilder sb = new StringBuilder();
        Map<String, Boolean> userLoadedMap = preloadedMap.computeIfAbsent(username, k -> new ConcurrentHashMap<>());
        preloaderList.forEach((packageName, preloader) -> {
            if (userLoadedMap.getOrDefault(packageName, Boolean.FALSE)) {
                sb.append(String.format("+ %s -- %s\n", preloader.packageName(), preloader.desc()));
                sb.append(String.format("  %s -> %s\n", preloader.sampleInput(), preloader.sampleOutput()));
                sb.append("\n");
            } else {
                sb.append(String.format("- %s %s\n", preloader.packageName(), preloader.desc()));
                sb.append(String.format("  %s -> %s\n", preloader.sampleInput(), preloader.sampleOutput()));
                sb.append("\n");
            }
        });
        return sb.toString();
    }

    private String preload(String username, String packageName) {
        List<String> scripts = Optional.ofNullable(preloaderList.get(packageName)).map(IJuggPreloader::getScripts) //
                .orElse(emptyList());

        if (isEmpty(scripts)) {
            throw new JuggRuntimeException(String.format("[system] packageName:%s not found!", packageName));
        }

        boolean isLoaded = preloadedMap.computeIfAbsent(username, k -> new ConcurrentHashMap<>()) //
                .getOrDefault(packageName, Boolean.FALSE);
        if (isLoaded) {
            throw new JuggRuntimeException(String.format("[system] packageName:%s is already preloaded!", packageName));
        }

        scripts.forEach(script -> evalKiller.eval(script, username));
        preloadedMap.computeIfAbsent(username, k -> new ConcurrentHashMap<>()) //
                .put(packageName, Boolean.TRUE);
        return String.format("preload: %s", packageName);
    }

    public Map<String, IJuggPreloader> getPreloaderList() {
        return preloaderList;
    }

    @Override
    public String name() {
        return "preload";
    }

    @Override
    public Map<String, String> patternToMessage() {
        return ImmutableMap.<String, String> builder() //
                .put("preload list", "list all preloaders.") //
                .put("preload {{packageName}}", "preload package") //
                .build();

    }
}
