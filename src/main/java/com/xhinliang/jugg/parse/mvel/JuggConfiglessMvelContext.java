package com.xhinliang.jugg.parse.mvel;

import java.util.HashMap;

import javax.annotation.Nullable;

/**
 * @author xhinliang
 */
public class JuggConfiglessMvelContext extends HashMap<String, Object> {

    private final JuggConfiglessMvelEvalKiller evalKiller;

    public JuggConfiglessMvelContext(JuggConfiglessMvelEvalKiller evalKiller) {
        this.evalKiller = evalKiller;
    }

    @Override
    public boolean containsKey(Object k) {
        if (k == null) {
            return false;
        }
        String key = (String) k;
        return super.containsKey(key) || getBean(key) != null;
    }

    @Override
    public Object get(Object k) {
        String key = (String) k;
        Object bean = super.get(key);
        if (bean == null) {
            bean = getBean(key);
        }
        return bean;
    }

    @Nullable
    private Object getBean(String beanName) {
        // Object bean = beanLoader.getBeanByName(beanName);
        if (beanName.equals("getBeanByName") || beanName.equals("getClassByName") || beanName.equals("getBeanByClass")) {
            return null;
        }
        Object bean = null;
        if (this.containsKey("getBeanByName")) {
            bean = evalKiller.evalWithoutContext(String.format("getBeanByName(\"%s\")", beanName));
        }
        Class<?> clazz = null;
        if (bean == null) {
            try {
                // clazz = beanLoader.getClassByName(beanName);
                String getClassEvalString = String.format("getClassByName(\"%s\")", beanName);
                if (this.containsKey("getClassByName")) {
                    clazz = (Class<?>) evalKiller.evalWithoutContext(getClassEvalString);
                    if (this.containsKey("getBeanByClass")) {
                        // bean = beanLoader.getBeanByClass(clazz);
                        bean = evalKiller.evalWithoutContext(String.format("getBeanByClass(%s)", getClassEvalString));
                    }
                } else {
                    clazz = Class.forName(beanName);
                }
            } catch (Exception e) {
                // pass
            }
        }
        if (bean != null) {
            return bean;
        }
        if (clazz != null) {
            return clazz;
        }
        return null;
    }
}
