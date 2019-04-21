package com.xhinliang.jugg.plugin.dump;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

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
public class JuggDumpHandler implements IJuggHandler, JuggHelpable {

    private final IJuggEvalKiller evalKiller;

    private JuggEvalDumpService contextDumpService = JuggEvalDumpServiceImpl.getInstance();

    private SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    public JuggDumpHandler(IJuggEvalKiller evalKiller) {
        this.evalKiller = evalKiller;
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

        if (command.equals("dump save")) {
            return save(context.getJuggUser().getUsername());
        }

        if (command.equals("dump list")) {
            return list(context.getJuggUser().getUsername());
        }

        if (command.startsWith("dump rm")) {
            String[] spliced = command.split(" ");
            if (spliced.length == 3) {
                return remove(context.getJuggUser().getUsername(), spliced[2]);
            } else {
                throw new JuggRuntimeException("[system] load syntax error!");
            }
        }

        if (command.startsWith("dump load")) {
            String[] spliced = command.split(" ");
            if (spliced.length == 3) {
                return load(context.getJuggUser().getUsername(), spliced[2]);
            } else {
                throw new JuggRuntimeException("[system] load syntax error!");
            }
        }

        return null;
    }

    private String list(String username) {
        StringBuilder sb = new StringBuilder("list of available dump files.\n");
        List<Long> fileNameToCreateTime = contextDumpService.list(username);
        fileNameToCreateTime.forEach((id) -> {
            sb.append(String.format("* %d -> %s\n", id, timeToString(id)));
            sb.append("\n");
        });
        return sb.toString();
    }

    private String save(String username) {
        Map context = evalKiller.getContext(username);
        // noinspection unchecked
        long id = contextDumpService.save(username, context);
        return "saved  " + id + ".";
    }

    private String load(String username, String id) {
        Map loadedContext = contextDumpService.load(username, Long.parseLong(id));
        // noinspection unchecked
        evalKiller.getContext(username).putAll(loadedContext);
        return "loaded " + id + ".";
    }

    private String remove(String username, String id) {
        contextDumpService.remove(username, Long.parseLong(id));
        return "remove " + id;
    }

    private String timeToString(long timeMillis) {
        return dateTimeFormatter.format(new Date(timeMillis));
    }

    @Override
    public String name() {
        return "preload";
    }

    @Override
    public Map<String, String> patternToMessage() {
        return ImmutableMap.<String, String> builder() //
                .put("dump list", "list available files.") //
                .put("dump load {{fileName}}", "load file.") //
                .put("dump rm {{fileName}}", "rm file.") //
                .build();
    }
}
