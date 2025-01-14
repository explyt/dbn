/*
 * Copyright 2024 Oracle and/or its affiliates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dbn.execution.java.wrapper;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public final class TypeMappingsManager {
    private final AtomicReference<String> lastFileHash = new AtomicReference<>("");
    private volatile Map<String, SqlType> dataTypeMap = Collections.emptyMap();
    private volatile Set<String> unsupportedTypes = Collections.emptySet();
    private static final String TEMPLATE_NAME = "DBN - OJVM TypeDefinitions";
    private final Project project;

    private TypeMappingsManager(Project project) {
        this.project = project;
        loadMappingsIfChanged();
    }

    public static TypeMappingsManager getInstance(Project project) {
        return project.getService(TypeMappingsManager.class);
    }

    private String calculateFileHash() {
        try {
            FileTemplate template = FileTemplateManager.getInstance(project).getCodeTemplate(TEMPLATE_NAME);
            String content = template.getText();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private synchronized void loadMappingsIfChanged() {
        String currentHash = calculateFileHash();
        if (!currentHash.equals(lastFileHash.get())) {
            try {
                FileTemplate template = FileTemplateManager.getInstance(project).getCodeTemplate(TEMPLATE_NAME);
                String content = template.getText();
                Map<String, SqlType> newMap = parseTemplateContent(content);
                dataTypeMap = Collections.unmodifiableMap(newMap);
                lastFileHash.set(currentHash);
            } catch (Exception e) {
                System.err.println("Failed to load type mappings: " + e.getMessage());
            }
        }
    }

    private Map<String, SqlType> parseTemplateContent(String content) {
        Map<String, SqlType> newMap = new HashMap<>();
        Set<String> newUnsupportedTypes = new HashSet<>();

        // Parse unsupported types list
        Pattern unsupportedPattern = Pattern.compile("#set\\(\\$unsupportedTypeSet\\s*=\\s*\\[(.*?)]\\)", Pattern.DOTALL);
        Matcher unsupportedMatcher = unsupportedPattern.matcher(content);
        if (unsupportedMatcher.find()) {
            String unsupportedList = unsupportedMatcher.group(1);
            Arrays.stream(unsupportedList.split(","))
                    .map(String::trim)
                    .map(s -> s.replaceAll("\"", ""))
                    .filter(s -> !s.isEmpty())
                    .forEach(newUnsupportedTypes::add);
        }

        Pattern pattern = Pattern.compile("\\$dataTypeMap\\.put\\(\"([^\"]+)\",\\s*\\{([^}]+)}\\)");
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String className = matcher.group(1);
            String properties = matcher.group(2);

            // Parse the properties
            Map<String, String> typeInfo = parseProperties(properties);

            if (typeInfo.containsKey("unsupported") && "true".equals(typeInfo.get("unsupported"))) {
                newUnsupportedTypes.add(className);
                continue;
            }

            SqlType sqlType = new SqlType(
                    typeInfo.get("sqlTypeName"),
                    typeInfo.getOrDefault("transformerPrefix", ""),
                    typeInfo.getOrDefault("transformerSuffix", "")
            );

            newMap.put(className, sqlType);
        }
        unsupportedTypes = Collections.unmodifiableSet(newUnsupportedTypes);

        return newMap;
    }

    private Map<String, String> parseProperties(String properties) {
        Map<String, String> result = new HashMap<>();
        Pattern propPattern = Pattern.compile("\"([^\"]+)\":\\s*\"([^\"]+)\"");
        Matcher propMatcher = propPattern.matcher(properties);

        while (propMatcher.find()) {
            String key = propMatcher.group(1);
            String value = propMatcher.group(2);
            result.put(key, value);
        }

        return result;
    }

    public Map<String, SqlType> getDataTypeMap() {
        loadMappingsIfChanged();
        return dataTypeMap;
    }

    public Set<String> getUnsupportedTypes() {
        loadMappingsIfChanged();
        return unsupportedTypes;
    }
}