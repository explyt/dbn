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

package com.dbn.code.common.lookup;

import com.dbn.code.common.completion.CodeCompletionContext;

import javax.swing.Icon;

public class AliasLookupItemBuilder extends LookupItemBuilder {
    private final CharSequence text;
    private final boolean definition;

    public AliasLookupItemBuilder(CharSequence text, boolean definition) {
        this.text = text;
        this.definition = definition;
    }

    @Override
    public String getTextHint() {
        return definition ? "alias def" : "alias ref";
    }

    @Override
    public boolean isBold() {
        return false;
    }

    @Override
    public CharSequence getText(CodeCompletionContext completionContext) {
        return text;
    }

    @Override
    public Icon getIcon() {
        return null;
    }
}