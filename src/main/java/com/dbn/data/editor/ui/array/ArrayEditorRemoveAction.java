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

package com.dbn.data.editor.ui.array;

import com.dbn.common.icon.Icons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import static com.dbn.nls.NlsResources.txt;

class ArrayEditorRemoveAction extends ArrayEditorAction {
    ArrayEditorRemoveAction() {
        super(txt("app.data.action.RemoveValue"), null, Icons.ARRAY_CELL_EDIT_REMOVE);
        //setShortcutSet(Keyboard.createShortcutSet(KeyEvent.VK_MINUS, UserInterface.ctrlDownMask()));
        //setShortcutSet(Keyboard.createShortcutSet(KeyEvent.VK_SUBTRACT, UserInterface.ctrlDownMask()));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ArrayEditorList list = getArrayEditorList(e);
        if (list == null) return;

        list.removeRow();
    }
}
