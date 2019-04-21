package com.xhinliang.jugg.parse.mvel;

import java.util.HashMap;

import javax.annotation.Nullable;

import com.xhinliang.jugg.loader.IBeanLoader;
import com.xhinliang.jugg.plugin.alias.AliasServiceFileImpl;

/**
 * @author xhinliang
 */
public class JuggMvelContext extends HashMap<String, Object> {

    private IBeanLoader beanLoader;

    public JuggMvelContext(IBeanLoader beanLoader) {
        this.beanLoader = beanLoader;
    }

    @Override
    public boolean containsKey(Object k) {
        if (k == null) {
            return false;
        }
        String key = (String) k;
        if (super.containsKey(key)) {
            return true;
        }
        Object bean = getBean(key);
        return bean != null;
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
        beanName = AliasServiceFileImpl.instance().getTargetRealNameIfNeed(beanName);
        Object bean = beanLoader.getBeanByName(beanName);
        Class<?> clazz = null;
        if (bean == null) {
            try {
                clazz = beanLoader.getClassByName(beanName);
                bean = beanLoader.getBeanByClass(clazz);
            } catch (ClassNotFoundException e) {
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
