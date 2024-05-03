package com.dbn.common.expression;

import lombok.SneakyThrows;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class ExpressionEvaluationManagerTest {

    @Test
    @SneakyThrows
    public void groovyRegex() {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("groovy");

        Boolean result = (Boolean) engine.eval("'hello' ==~ /(?i)hello/");
        System.out.println(result); // prints: true

        result = (Boolean) engine.eval("'hello' ==~ /world/");
        System.out.println(result); // prints: false
    }


}