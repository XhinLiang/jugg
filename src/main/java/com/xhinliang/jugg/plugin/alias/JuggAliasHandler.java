package com.xhinliang.jugg.plugin.alias;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.xhinliang.jugg.context.CommandContext;
import com.xhinliang.jugg.exception.JuggRuntimeException;
import com.xhinliang.jugg.handler.IJuggHandler;
import com.xhinliang.jugg.loader.IBeanLoader;
import com.xhinliang.jugg.plugin.help.JuggHelpable;
import com.xhinliang.jugg.util.JsonMapperUtils;

/**
 * @author xhinliang
 */
public class JuggAliasHandler implements IJuggHandler, JuggHelpable {

    private IBeanLoader beanLoader;

    public JuggAliasHandler(IBeanLoader beanLoader) {
        this.beanLoader = beanLoader;
    }

    @Override
    public void handle(CommandContext context) {
        @Nullable
        String result = generateResult(context);
        if (result != null) {
            context.setResult(result);
            context.setShouldEnd(true);
        }
    }

    @Nullable
    public String generateResult(CommandContext context) {
        String command = context.getCommand();
        if (!command.startsWith("alias ")) {
            return null;
        }

        String[] spliced = command.split(" ");
        if (spliced.length == 4) {
            handleAliasCommand(spliced);
            return "done";
        }

        if (spliced.length == 2) {
            return JsonMapperUtils.toPrettyJson(handleGetAliasCommand(spliced));
        }
        throw new JuggRuntimeException("[system] alias syntax error!");
    }

    private void handleAliasCommand(String[] spliced) {
        AliasService aliasService = AliasServiceFileImpl.instance();
        String targetOrMethod = spliced[1];
        String alia = spliced[2];
        String real = spliced[3];

        if (targetOrMethod.equals("target")) {
            try {
                aliasService.addLocalTargetAlia(alia, real, beanLoader);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Object handleGetAliasCommand(String[] spliced) {
        AliasService aliasService = AliasServiceFileImpl.instance();
        String targetOrMethod = spliced[1];

        if (targetOrMethod.equals("target")) {
            return aliasService.getTargetAlias();
        }

        throw new JuggRuntimeException("[system] alias syntax error, command [" + targetOrMethod + "] not allow!");
    }

    @Override
    public String name() {
        return "alias";
    }

    @Override
    public Map<String, String> patternToMessage() {
        return ImmutableMap.<String, String> builder() //
                .put("alias target", "list all current alias.") //
                .put("alias target {{aliaName}} {{realName}}", "list help messages of this handler") //
                .build();
    }
}
