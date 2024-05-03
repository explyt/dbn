package com.dbn.common.expression;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlToGroovyExpressionConverter {
    private static final Map<String, String> cache = new ConcurrentHashMap<>();

    public static String cachedSqlToGroovy(String expression) {
        return cache.computeIfAbsent(expression, e -> sqlToGroovy(e));
    }

    public static String sqlToGroovy(String expression) {
        expression = expression.replaceAll("(?i)\\bOR\\b", "||");
        expression = expression.replaceAll("(?i)\\bAND\\b", "&&");
        expression = expression.replaceAll("(?i)\\bIS\\s+NULL\\b", "== null");
        expression = expression.replaceAll("(?i)\\bIS\\s+NOT\\s+NULL\\b", "!= null");

        expression = replace_EQUALS(expression);
        expression = replace_NOT_LIKE(expression);
        expression = replace_LIKE(expression);
        expression = replace_NOT_IN(expression);
        expression = replace_IN(expression);
        expression = expression.replaceAll("(?i)\\bNOT\\b", "!");

        return expression.replaceAll("\\s+", " ").trim();
    }

    private static String replace_EQUALS(String expression) {
        Pattern pattern = Pattern.compile("(?<!([=<>!]))=(?!=)");
        Matcher matcher = pattern.matcher(expression);
        expression = matcher.replaceAll("==");
        return expression;
    }

    private static String replace_NOT_LIKE(String expression) {
        Pattern p = Pattern.compile("(?i)(\\w+)\\s+NOT\\s+LIKE\\s+('[^']*')");
        Matcher m = p.matcher(expression);
        StringBuilder result = new StringBuilder();
        while (m.find()) {
            String name = m.group(1);
            String value = "/(?i)" + m.group(2)
                    .replaceAll("'", "")
                    .replaceAll("[%*]", ".*") + "/";
            String transformed = String.format("!(%s ==~ %s)", name, value);
            m.appendReplacement(result, transformed);
        }
        m.appendTail(result);
        expression = result.toString();
        return expression;
    }

    private static String replace_LIKE(String expression) {
        Pattern p = Pattern.compile("(?i)(\\w+)\\s+LIKE\\s+('[^']*')");
        Matcher m = p.matcher(expression);
        StringBuilder result = new StringBuilder();
        while (m.find()) {
            String name = m.group(1);
            String value = "/(?i)" + m.group(2)
                    .replaceAll("'", "")
                    .replaceAll("[%*]", ".*") + "/";
            String transformed = String.format("%s ==~ %s", name, value);
            m.appendReplacement(result, transformed);
        }
        m.appendTail(result);
        expression = result.toString();
        return expression;
    }

    private static String replace_NOT_IN(String expression) {
        Pattern p = Pattern.compile("(?i)(\\w+)\\s+NOT\\s+IN\\s+\\((.*?)\\)");
        Matcher m = p.matcher(expression);
        StringBuilder result = new StringBuilder();
        while (m.find()) {
            String name = m.group(1);
            String values = m.group(2);
            String transformed = String.format("![%s].contains(%s)", values, name);
            m.appendReplacement(result, transformed);
        }
        m.appendTail(result);
        expression = result.toString();
        return expression;
    }

    private static String replace_IN(String expression) {
        Pattern p = Pattern.compile("(?i)(\\w+)\\s+IN\\s+\\((.*?)\\)");
        Matcher m = p.matcher(expression);
        StringBuilder result = new StringBuilder();
        while (m.find()) {
            String name = m.group(1);
            String values = m.group(2);
            String transformed = String.format("[%s].contains(%s)", values, name);
            m.appendReplacement(result, transformed);
        }
        m.appendTail(result);
        expression = result.toString();
        return expression;
    }
}
