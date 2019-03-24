package com.xhinliang.jugg.plugin.insight;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.google.common.base.Joiner;

/**
 * @author xhinliang
 */
public class JuggInsightServiceImpl implements JuggInsightService {

    private final Joiner spaceJoiner = Joiner.on(" ");
    private final Joiner commaJoiner = Joiner.on(", ");

    private final List<Method> modifierMethods = allMethods(Modifier.class).stream() //
            .filter(m -> m.getName().startsWith("is")) //
            .sorted(Comparator.comparing(Method::getName)) //
            .collect(toImmutableList());

    @Override
    public List<String> methods(Object targetObject) {
        return getMethodDescList(targetObject);
    }

    @Override
    public List<String> fields(Object targetObject) {
        return getFieldsDescList(targetObject);
    }

    private List<String> getMethodDescList(Object targetObject) {
        return allMethods(targetObject).stream() //
                .map(this::methodToString) //
                .collect(toImmutableList());
    }

    private List<String> getFieldsDescList(Object targetObject) {
        return allFields(targetObject).stream() //
                .map(f -> fieldToString(targetObject, f)) //
                .collect(toImmutableList());
    }

    private List<Method> allMethods(Object targetObject) {
        if (targetObject instanceof Class) {
            return Arrays.stream(((Class) targetObject).getDeclaredMethods()) //
                    .filter(m -> Modifier.isStatic(m.getModifiers())) //
                    .collect(toImmutableList());
        }
        return Arrays.stream(targetObject.getClass() //
                .getDeclaredMethods()) //
                .collect(toImmutableList());
    }

    private List<Field> allFields(Object targetObject) {
        if (targetObject instanceof Class) {
            return Arrays.stream(((Class) targetObject).getDeclaredFields()) //
                    .filter(m -> Modifier.isStatic(m.getModifiers())) //
                    .collect(toImmutableList());
        }
        return Arrays.stream(targetObject.getClass() //
                .getDeclaredFields()) //
                .collect(toImmutableList());
    }

    private String methodToString(Method targetMethod) {
        String modString = getModifierString(targetMethod.getModifiers());
        String returnType = targetMethod.getReturnType().getSimpleName();
        String methodName = targetMethod.getName();
        Stream<Parameter> parameters = Arrays.stream(targetMethod.getParameters());
        String innerParameterString = commaJoiner.join(parameters.map(this::parameterToString).toArray());
        String parametersString = "(" + innerParameterString + ")";
        return spaceJoiner.join(modString, returnType, methodName, parametersString);
    }

    private String parameterToString(Parameter parameter) {
        String parameterType = parameter.getType().getSimpleName();
        String name = parameter.getName();
        return spaceJoiner.join(parameterType, name);
    }

    private String fieldToString(Object target, Field field) {
        String modString = getModifierString(field.getModifiers());
        String fieldType = field.getType().getSimpleName();
        String fieldName = field.getName();
        String value;
        field.setAccessible(true);
        try {
            value = field.get(target) + "";
        } catch (Exception e) {
            value = "getFieldError:" + e.getClass().getSimpleName();
        }
        return spaceJoiner.join(modString, fieldType, fieldName, value);
    }

    private String getModifierString(int mod) {
        List<String> modStrings = modifierMethods.stream() //
                .map(m -> {
                    try {
                        Boolean is;
                        is = (Boolean) m.invoke(Modifier.class, mod);
                        if (is) {
                            return m.getName().substring(2).toLowerCase();
                        }
                    } catch (Exception e) {
                        return null;
                    }
                    return null;
                }) //
                .filter(Objects::nonNull) //
                .collect(toList());

        return spaceJoiner.join(modStrings);
    }
}
