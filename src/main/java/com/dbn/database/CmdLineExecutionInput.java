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
