package com.xhinliang.jugg.loader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Impl the beanLoader so that JuggWebSocketServer can load a bean or a class what request required.
 *
 * For a specific $beanName
 * - if getBeanByName return nonnull value,
 *   JuggWebSocketServer will call this method as a object method.
 *
 * - if getBeanByName return null value,
 *   JuggWebSocketServer will call getClassByName and get the Class of this target, we call $beanClass
 *
 *   - if getBeanByClass($beanClass) return nonnull, JuggWebSocketServer will call this method as object method.
 *   - else JuggWebSocketServer will call this method as a static method.
 */
public interface IBeanLoader {

    @Nullable
    Object getBeanByName(String name);

    @Nonnull
    Class<?> getClassByName(String name) throws ClassNotFoundException;

    @Nullable
    Object getBeanByClass(@Nonnull Class<?> clazz);
}
