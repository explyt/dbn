package com.dbn.common;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.dbn.common.util.Commons.nvl;
import static com.dbn.common.util.Unsafe.cast;

@UtilityClass
public class Reflection {
    private static final Map<Class, Class> enclosingClasses = new ConcurrentHashMap<>();
    private static final Map<Class, String> classNames = new ConcurrentHashMap<>();
    private static final Map<Class, String> simpleClassNames = new ConcurrentHashMap<>();

    public static Class<?> getEnclosingClass(Class clazz) {
        return enclosingClasses.computeIfAbsent(clazz, c -> nvl(c.getEnclosingClass(), c));
    }

    public static String getClassName(Class clazz) {
        return classNames.computeIfAbsent(clazz, c -> c.getName());
    }

    public static String getSimpleClassName(Class clazz) {
        return simpleClassNames.computeIfAbsent(clazz, c -> c.getSimpleName());
    }

    @SneakyThrows
    public static <T> T invokeMethod(Object object, Method method, Object... args) {
        return cast(method.invoke(object, args));
    }

    @SneakyThrows
    public static <T> T invokeMethod(Object object, String methodName, Object... args) {
        Class[] argTypes = Arrays.stream(args).map(Object::getClass).toArray(Class[]::new);
        Method method = findMethod(object.getClass(), methodName, argTypes);
        if (method == null) return null;

        return invokeMethod(object, method, args);
    }

    @Nullable
    public static Method findMethod(Class<?> objectClass, String methodName, Class[] argTypes) {
        try {
            return objectClass.getMethod(methodName, argTypes);
        } catch (Throwable e) {
            return null;
        }
    }

}
