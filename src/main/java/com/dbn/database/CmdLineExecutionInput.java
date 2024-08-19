package com.dbn.database;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class CmdLineExecutionInput {
    private final StringBuilder content;
    private final List<String> command;
    private final Map<String, String> environmentVars;

    public CmdLineExecutionInput(@NotNull String content) {
        this.content = new StringBuilder(content);
        this.command = new ArrayList<>();
        this.environmentVars = new HashMap<>();

    }

    public void addEnvironmentVariable(String key, String value) {
        environmentVars.put(key, value);
    }

    public String getTextContent() {
        return content.toString();
    }

    @NotNull
    public String getLineCommand() {
        StringBuilder lineCommand = new StringBuilder();
        for (String arg : command) {
            lineCommand.append(arg).append(" ");
        }

        return lineCommand.toString();
    }
}
