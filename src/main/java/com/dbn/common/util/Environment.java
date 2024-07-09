package com.dbn.common.util;

import com.intellij.openapi.application.ApplicationInfo;
import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class Environment {
    private static final Map<String, String> variables = new ConcurrentHashMap<>();

    public static boolean isIdeNewerThan(String targetVersion) {
        String currentVersion = ApplicationInfo.getInstance().getFullVersion();
        return isVersionGreaterThan(currentVersion, targetVersion);
    }


    private static boolean isVersionGreaterThan(String currentVersion, String targetVersion) {
        String[] currentParts = currentVersion.split("\\.");
        String[] targetParts = targetVersion.split("\\.");

        int currentMajor = Integer.parseInt(currentParts[0]);
        int targetMajor = Integer.parseInt(targetParts[0]);

        if (currentMajor > targetMajor) return true;
        if (currentMajor < targetMajor) return false;

        int currentMinor = Integer.parseInt(currentParts[1]);
        int targetMinor = Integer.parseInt(targetParts[1]);

        return currentMinor > targetMinor;
    }


    public static String getVariable(String key) {
        return variables.computeIfAbsent(key, k -> loadVariable(k));
    }

    private static String loadVariable(String name) {
        String property = System.getProperty(name);
        if (Strings.isNotEmptyOrSpaces(property)) return property.trim();

        Map<String, String> environmentVariables = System.getenv();
        for (String variableName : environmentVariables.keySet()) {
            if (variableName.equalsIgnoreCase(name)) {
                return environmentVariables.get(variableName);
            }
        }

        return null;
    }
}
