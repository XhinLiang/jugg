package com.xhinliang.jugg.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Joiner;

/**
 * @author xhinliang
 */
public class JuggCallingHelper {

    public String m(Object targetObject) {
        return Joiner.on("\n").join(methods(targetObject));
    }

    public String f(Object targetObject) {
        return Joiner.on("\n").join(fields(targetObject));
    }

    public List<String> methods(Object targetObject) {
        return getMethodDescList(targetObject, false);
    }

    public List<String> staticMethods(Object targetObject) {
        return getMethodDescList(targetObject, true);
    }

    public List<String> fields(Object targetObject) {
        return getFieldsDescList(targetObject, false);
    }

    public List<String> staticFields(Object targetObject) {
        return getFieldsDescList(targetObject, true);
    }

    private List<String> getMethodDescList(Object targetObject, boolean staticMethod) {
        return allMethods(targetObject).stream() //
                .filter(m -> staticMethod == Modifier.isStatic(m.getModifiers())) //
                .map(JuggCallingHelper::methodToString) //
                .collect(Collectors.toList());
    }

    private List<String> getFieldsDescList(Object targetObject, boolean staticField) {
        return allFields(targetObject).stream() //
                .filter(f -> staticField == Modifier.isStatic(f.getModifiers())) //
                .map(f -> JuggCallingHelper.fieldToString(targetObject, f)) //
                .collect(Collectors.toList());
    }

    private List<Method> allMethods(Object targetObject) {
        return Arrays.stream(targetObject.getClass().getDeclaredMethods()) //
                .collect(Collectors.toList());
    }

    private List<Field> allFields(Object targetObject) {
        return Arrays.stream(targetObject.getClass().getDeclaredFields()) //
                .collect(Collectors.toList());
    }

    private static String methodToString(Method targetMethod) {
        String returnType = targetMethod.getReturnType().getSimpleName();
        String methodName = targetMethod.getName();
        Stream<Parameter> parameters = Arrays.stream(targetMethod.getParameters());
        String innerParameterString = Joiner.on(", ").join(parameters.map(JuggCallingHelper::parameterToString).toArray());
        String parametersString = "(" + innerParameterString + ")";
        return Joiner.on(" ").join(returnType, methodName, parametersString);
    }

    private static String parameterToString(Parameter parameter) {
        String parameterType = parameter.getType().getSimpleName();
        String name = parameter.getName();
        return Joiner.on(" ").join(parameterType, name);
    }

    private static String fieldToString(Object target, Field field) {
        String fieldType = field.getType().getTypeName();
        String fieldName = field.getName();
        String value;
        field.setAccessible(true);
        try {
            value = target.getClass().getField(fieldName).get(target) + "";
        } catch (Exception e) {
            value = "getFieldError:" + e.getClass().getSimpleName();
        }
        return Joiner.on(" ").join(fieldType, fieldName, value);
    }
}
