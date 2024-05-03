package com.dbn.common.util;

import com.dbn.common.dispose.Failsafe;
import com.dbn.common.thread.*;
import com.dbn.diagnostics.Diagnostics;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.dbn.diagnostics.Diagnostics.conditionallyLog;

@Slf4j
@UtilityClass
public final class Traces {

    public static final Set<String> SKIPPED_CALL_STACK_CLASSES = new HashSet<>(Arrays.asList(
            Traces.class.getName(),
            ThreadInfo.class.getName(),
            ThreadMonitor.class.getName(),
            Synchronized.class.getName(),
            Background.class.getName(),
            Progress.class.getName(),
            Failsafe.class.getName()));

    public static boolean isCalledThrough(String ... oneOfClassesNames) {
        StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
        try {
            for (int i = 3; i < callStack.length; i++) {
                StackTraceElement stackTraceElement = callStack[i];
                String className = stackTraceElement.getClassName();
                for (String name : oneOfClassesNames) {
                    if (Objects.equals(name, className)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            conditionallyLog(e);
            return false;
        }
        return false;
    }
    public static boolean isCalledThrough(Class ... oneOfClasses) {
        StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
        try {
            for (int i = 3; i < callStack.length; i++) {
                StackTraceElement stackTraceElement = callStack[i];
                String className = stackTraceElement.getClassName();
                for (Class clazz : oneOfClasses) {
                    if (Objects.equals(clazz.getName(), className) /*|| clazz.isAssignableFrom(Class.forName(className))*/) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            conditionallyLog(e);
            return false;
        }
        return false;
    }

    public static boolean isCalledThrough(Class clazz, String methodName) {
        StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
        try {
            for (int i = 3; i < callStack.length; i++) {
                StackTraceElement stackTraceElement = callStack[i];
                String className = stackTraceElement.getClassName();
                if (Objects.equals(clazz.getName(), className) /*|| clazz.isAssignableFrom(Class.forName(className))*/) {
                    String methName = stackTraceElement.getMethodName();
                    if (Objects.equals(methodName, methName)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            conditionallyLog(e);
            return false;
        }
        return false;
    }

    public static StackTraceElement[] diagnosticsCallStack() {
        if (!Diagnostics.isDeveloperMode()) return null;

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        return Arrays
                .stream(stackTrace)
                .filter(st -> !SKIPPED_CALL_STACK_CLASSES.contains(st.getClassName()))
                .toArray(StackTraceElement[]::new);
    }
}
