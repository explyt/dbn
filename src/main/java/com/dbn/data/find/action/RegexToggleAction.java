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
package com.dbn.data.find.action;

import com.dbn.data.find.DataSearchComponent;
import com.intellij.find.FindModel;
import com.intellij.find.FindSettings;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import static com.dbn.nls.NlsResources.txt;

public class RegexToggleAction extends DataSearchHeaderToggleAction {

    public RegexToggleAction(DataSearchComponent searchComponent) {
        super(
                searchComponent,
                txt("app.data.action.FindRegex"),
                AllIcons.Actions.Regex,
                AllIcons.Actions.RegexHovered,
                AllIcons.Actions.RegexSelected);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return getFindModel().isRegularExpressions();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        FindModel findModel = getFindModel();
        findModel.setRegularExpressions(state);
        if (state) {
            findModel.setWholeWordsOnly(false);
        }
        FindSettings.getInstance().setLocalRegularExpressions(state);
    }
}
