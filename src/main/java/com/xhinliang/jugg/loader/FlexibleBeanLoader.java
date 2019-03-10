package com.xhinliang.jugg.loader;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import com.xhinliang.jugg.util.ClassFinder;

/**
 * IBeanLoader with flexible calling.
 *
 * FlexibleBeanLoader can auto find Java Class and register it.
 * In most case, you can replace FJCN with SimpleClassName.
 *
 * for example, you can call `@Maps@newHashMap()` instead of `@com.google.common.collect.Maps@newHashMap()`
 *
 * @author xhinliang
 */
public abstract class FlexibleBeanLoader implements IBeanLoader {

    private Map<String, String> clazzMap;

    public FlexibleBeanLoader() {
        this(new HashMap<>());
    }

    public FlexibleBeanLoader(Map<String, String> clazzMap) {
        this.clazzMap = clazzMap;
        init();
    }

    public abstract Object getBeanByClass(@Nonnull Class<?> clazz);

    @Nullable
    protected abstract Object getActualBean(String name);

    private void init() {
        ClassFinder.findClasses(s -> {
            String simpleClassName = s.substring(s.lastIndexOf(".") + 1);
            clazzMap.putIfAbsent(simpleClassName, s);
            return true;
        });
        Configuration configuration = new ConfigurationBuilder() //
                .setUrls(Stream.of(ClasspathHelper.forPackage("com"), ClasspathHelper.forPackage("org"), ClasspathHelper.forPackage("net")) //
                        .flatMap(Collection::stream) //
                        .collect(Collectors.toSet())) //
                .setScanners(new SubTypesScanner(false));
        Reflections reflections = new Reflections(configuration);
        reflections.getAllTypes().forEach(s -> {
            String simpleClassName = s.substring(s.lastIndexOf(".") + 1);
            clazzMap.putIfAbsent(simpleClassName, s);
        });
    }

    @Nullable
    @Override
    public Object getBeanByName(String name) {
        Object bean = getActualBean(name);
        if (bean == null) {
            String classSimpleName = name.substring(0, 1).toUpperCase() + name.substring(1);
            String className = clazzMap.get(classSimpleName);
            if (StringUtils.isNotBlank(className)) {
                try {
                    Class<?> clazz = getClassByName(className);
                    return getBeanByClass(clazz);
                } catch (ClassNotFoundException e) {
                    return null;
                }
            }
        }
        return bean;
    }

    @Nonnull
    @Override
    public Class<?> getClassByName(String name) throws ClassNotFoundException {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException classNotFound) {
            String retryClassName = clazzMap.getOrDefault(name, name);
            return Class.forName(retryClassName);
        }
    }
}
