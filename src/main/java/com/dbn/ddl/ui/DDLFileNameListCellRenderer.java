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

package com.dbn.ddl.ui;

import com.dbn.ddl.DDLFileNameProvider;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

import javax.swing.JList;

public class DDLFileNameListCellRenderer extends ColoredListCellRenderer<DDLFileNameProvider> {
    @Override
    protected void customizeCellRenderer(@NotNull JList list, DDLFileNameProvider value, int index, boolean selected, boolean hasFocus) {

        append(value.getFilePattern(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        append(" (" + value.getDdlFileType().getDescription() + ") ", SimpleTextAttributes.GRAY_ATTRIBUTES);

        //Module module = ProjectRootManager.getInstance(psiFile.getProject()).getFileIndex().getModuleForFile(virtualFile);
        //append(" - module " + module.getName(), SimpleTextAttributes.GRAYED_ATTRIBUTES);

        setIcon(value.getDdlFileType().getLanguageFileType().getIcon());
    }
}