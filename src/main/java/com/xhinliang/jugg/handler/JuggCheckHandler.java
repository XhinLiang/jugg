package com.xhinliang.jugg.handler;

import java.util.function.Function;

import com.xhinliang.jugg.context.CommandContext;

/**
 * @author xhinliang
 */
public class JuggCheckHandler implements IJuggHandler {

    private Function<CommandContext, Boolean> checker;

    public JuggCheckHandler(Function<CommandContext, Boolean> checker) {
        this.checker = checker;
    }

    @Override
    public void handle(CommandContext context) {
        boolean checkResult = checker.apply(context);
         if (!checkResult) {
            String message = String.format("[system] command check failed, %s not allow call %s!", context.getJuggUser().getUsername(),
                    context.getCommand());
            context.setResult(message);
            context.setShouldEnd(true);
        }
    }
}
