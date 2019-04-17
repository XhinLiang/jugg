package com.xhinliang.jugg.parse.mvel;

import org.mvel2.MVEL;

import com.xhinliang.jugg.context.CommandContext;
import com.xhinliang.jugg.loader.IBeanLoader;
import com.xhinliang.jugg.parse.IJuggEvalKiller;
import com.xhinliang.jugg.parse.ognl.JuggOgnlContext;

import ognl.OgnlContext;

/**
 * @author xhinliang <xhinliang@gmail.com>
 * Created on 2019-04-16
 */
public class JuggMvelEvalKiller implements IJuggEvalKiller {

    private OgnlContext globalContext;

    public JuggMvelEvalKiller(IBeanLoader beanLoader) {
        this.globalContext = new JuggOgnlContext(beanLoader);
    }

    @Override
    public Object eval(CommandContext commandContext) {
        String command = commandContext.getCommand();
        return MVEL.eval(command, globalContext);
    }
}
