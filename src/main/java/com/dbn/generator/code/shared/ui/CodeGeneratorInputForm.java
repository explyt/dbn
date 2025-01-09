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

package com.dbn.generator.code.shared.ui;

import com.dbn.common.ui.form.DBNFormBase;
import com.dbn.generator.code.shared.CodeGeneratorInput;
import com.intellij.openapi.ui.ValidationInfo;

import lombok.Getter;

@Getter
public abstract class CodeGeneratorInputForm<I extends CodeGeneratorInput> extends DBNFormBase {
    private final I input;
    private CodeGeneratorInputDialog dialog;

    public CodeGeneratorInputForm(CodeGeneratorInputDialog dialog, I input) {
        super(dialog);
        this.input = input;
        this.dialog = dialog;
    }

    public final void applyUserInput() {
        applyUserInput(input);
        //this.dialog.validate();
    }

    /**
     * Expected to apply all user inputs from the input-form fields to the {@link CodeGeneratorInput}
     * @param input the
     */
    protected abstract void applyUserInput(I input);
    
    public abstract ValidationInfo doValidate();
}
