package com.xhinliang.jugg.loader;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

/**
 * IBeanLoader with flexible calling.
 *
 * FlexibleBeanLoader can auto find Java Class and register it.
 * In most case, you can replace FQCN with SimpleClassName.
 *
 * for example
 *
 * In OgnlEvalKiller, you can call `@Maps@newHashMap()` instead of `@com.google.common.collect.Maps@newHashMap()`
 *
 * In MvelEvalKiller, you can call `Maps.newHashMap()` instead of `com.google.common.collect.Maps.newHashMap()`
 *
 * @author xhinliang
 */
public abstract class FlexibleBeanLoader implements IBeanLoader {

    private Map<String, String> simpleClassNameMap;

    private List<String> preferClassPrefix;

    public FlexibleBeanLoader() {
        this(new HashMap<>());
    }

    public FlexibleBeanLoader(List<String> preferClassPrefix) {
        this(new HashMap<>(), preferClassPrefix);
    }

    public FlexibleBeanLoader(Map<String, String> simpleClassName2FqcnMap) {
        this(simpleClassName2FqcnMap, new ArrayList<>());
    }

    public FlexibleBeanLoader(Map<String, String> simpleClassNameMap, List<String> preferClassPrefix) {
        this.simpleClassNameMap = simpleClassNameMap;
        this.preferClassPrefix = preferClassPrefix;
        init();
    }

    public abstract Object getBeanByClass(@Nonnull Class<?> clazz);

    @Nullable
    protected abstract Object getActualBean(String name);

    @PostConstruct
    private void init() {
        Set<String> fqcnSet = new HashSet<>();
        ClassFinder.findClasses(fqcn -> {
            fqcnSet.add(fqcn);
            return true;
        });

        Configuration configuration = new ConfigurationBuilder() //
                .setUrls(Stream.of(ClasspathHelper.forPackage("com"), ClasspathHelper.forPackage("org"), ClasspathHelper.forPackage("net")) //
                        .flatMap(Collection::stream) //
                        .collect(toSet())) //
                .setScanners(new SubTypesScanner(false));

        Reflections reflections = new Reflections(configuration);
        Stream<String> anotherFqcnStream = reflections.getAllTypes().stream();

        Map<String, String> tempMap = Stream.concat(fqcnSet.stream(), anotherFqcnStream)
                .collect(toMap(this::getSimpleName, identity(), (fqcnA, fqcnB) -> {
                    boolean preferB = preferClassPrefix.stream() //
                            .anyMatch(fqcnB::startsWith);
                    return preferB ? fqcnB : fqcnA;
                }));
        tempMap.putAll(simpleClassNameMap);
        simpleClassNameMap = tempMap;
    }

    private String getSimpleName(String fqcn) {
        return fqcn.substring(fqcn.lastIndexOf(".") + 1);
    }

    @Nullable
    @Override
    public Object getBeanByName(String name) {
        Object bean = null;
        try {
            bean = getActualBean(name);
        } catch (Exception e) {
            // pass
        }
        if (bean == null) {
            String classSimpleName = name.substring(0, 1).toUpperCase() + name.substring(1);
            String className = simpleClassNameMap.get(classSimpleName);
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
            String retryClassName = simpleClassNameMap.getOrDefault(name, name);
            return Class.forName(retryClassName);
        }
    }

    @Nullable
    public String getFqcnBySimpleClassName(String simpleClassName) {
        return simpleClassNameMap.get(simpleClassName);
    }
}
