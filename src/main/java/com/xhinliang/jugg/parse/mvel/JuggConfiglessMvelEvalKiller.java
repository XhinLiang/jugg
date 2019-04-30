package com.xhinliang.jugg.parse.mvel;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.mvel2.MVEL;

import com.xhinliang.jugg.parse.IJuggEvalKiller;

/**
 * @author xhinliang <xhinliang@gmail.com>
 * Created on 2019-04-16
 */
public class JuggConfiglessMvelEvalKiller implements IJuggEvalKiller {

    private JuggConfiglessMvelContext globalContext;

    private HashMap<String, Object> localContext = new HashMap<>();

    public JuggConfiglessMvelEvalKiller() {
        this.globalContext = new JuggConfiglessMvelContext(this);
    }

    public Object eval(String command) {
        return MVEL.eval(command, localContext, globalContext);
    }

    @Override
    public Object eval(String command, String username) {
        return eval(command);
    }

    public Object evalWithoutContext(String command) {
        Map<String, Object> systemContext = new HashMap<>();
        globalContext.forEach((key, val) -> {
            if (key.equals("getBeanByName")) {
                systemContext.put(key, val);
            }
            if (key.equals("getClassByName")) {
                systemContext.put(key, val);
            }
            if (key.equals("getBeanByClass")) {
                systemContext.put(key, val);
            }
        });
        Map<String, Object> tempContext = new HashMap<>();
        return MVEL.eval(command, tempContext, systemContext);
    }

    @Nonnull
    @Override
    public Map getContext(String username) {
        return globalContext;
    }
}
