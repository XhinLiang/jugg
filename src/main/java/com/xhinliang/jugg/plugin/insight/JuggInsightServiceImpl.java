package com.xhinliang.jugg.plugin.insight;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Joiner;

/**
 * @author xhinliang
 */
public class JuggInsightServiceImpl implements JuggInsightService {

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
                .map(JuggInsightServiceImpl::methodToString) //
                .collect(Collectors.toList());
    }

    private List<String> getFieldsDescList(Object targetObject) {
        return allFields(targetObject).stream() //
                .map(f -> JuggInsightServiceImpl.fieldToString(targetObject, f)) //
                .collect(Collectors.toList());
    }

    private List<Method> allMethods(Object targetObject) {
        if (targetObject instanceof Class) {
            return Arrays.stream(((Class) targetObject).getDeclaredMethods()) //
                    .collect(Collectors.toList());
        }
        return Arrays.stream(targetObject.getClass() //
                .getDeclaredMethods()) //
                .collect(Collectors.toList());
    }

    private List<Field> allFields(Object targetObject) {
        if (targetObject instanceof Class) {
            return Arrays.stream(((Class) targetObject).getDeclaredFields()) //
                    .collect(Collectors.toList());
        }
        return Arrays.stream(targetObject.getClass() //
                .getDeclaredFields()) //
                .collect(Collectors.toList());
    }

    private static String methodToString(Method targetMethod) {
        String returnType = targetMethod.getReturnType().getSimpleName();
        String methodName = targetMethod.getName();
        Stream<Parameter> parameters = Arrays.stream(targetMethod.getParameters());
        String innerParameterString = Joiner.on(", ").join(parameters.map(JuggInsightServiceImpl::parameterToString).toArray());
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
