package com.xhinliang.jugg.parse.ognl;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.xhinliang.jugg.exception.JuggRuntimeException;
import com.xhinliang.jugg.loader.IBeanLoader;
import com.xhinliang.jugg.plugin.alias.AliasServiceFileImpl;

import ognl.DefaultTypeConverter;
import ognl.MemberAccess;
import ognl.OgnlContext;

/**
 * @author xhinliang
 */
public class JuggOgnlContext extends OgnlContext {

    private static final int CAPACITY = 1000;

    private IBeanLoader beanLoader;

    private static final MemberAccess DEFAULT_MEMBER_ACCESS = new MemberAccess() {

        @Override
        public Object setup(Map context, Object target, Member member, String propertyName) {
            return target;
        }

        @Override
        public void restore(Map context, Object target, Member member, String propertyName, Object state) {
            // pass
        }

        @Override
        public boolean isAccessible(Map context, Object target, Member member, String propertyName) {
            // access private method/field
            if (member instanceof AccessibleObject) {
                ((AccessibleObject) member).setAccessible(true);
            }
            return true;
        }
    };

    public JuggOgnlContext(IBeanLoader beanLoader) {
        this(beanLoader, new ConcurrentHashMap(CAPACITY));
    }

    public JuggOgnlContext(IBeanLoader beanLoader, Map contextMap) {
        super(DEFAULT_MEMBER_ACCESS, (className, context) -> {
            Class<?> clazz = beanLoader.getClassByName(AliasServiceFileImpl.instance().getTargetRealNameIfNeed(className));
            return clazz;
        }, new DefaultTypeConverter(), contextMap);
        this.beanLoader = beanLoader;
    }

    @Override
    public boolean containsKey(Object k) {
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

    private Object getBean(String beanName) {
        beanName = AliasServiceFileImpl.instance().getTargetRealNameIfNeed(beanName);
        Object bean = beanLoader.getBeanByName(beanName);
        if (bean == null) {
            try {
                Class<?> clazz = beanLoader.getClassByName(beanName);
                bean = beanLoader.getBeanByClass(clazz);
            } catch (ClassNotFoundException e) {
                // pass
            }
        }
        if (bean == null) {
            throw new JuggRuntimeException("bean not found, beanName: " + beanName);
        }
        return bean;
    }
}
