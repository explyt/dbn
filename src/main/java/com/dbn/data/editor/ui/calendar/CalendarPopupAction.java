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

package com.dbn.data.editor.ui.calendar;

import com.dbn.common.action.BasicAction;
import com.dbn.common.action.DataKeys;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.NlsActions.ActionDescription;
import com.intellij.openapi.util.NlsActions.ActionText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public abstract class CalendarPopupAction extends BasicAction {
    public CalendarPopupAction(@Nullable @ActionText String text, @Nullable @ActionDescription String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    @Nullable
    protected CalendarPopupProviderForm getCalendarForm(@NotNull AnActionEvent e) {
        return e.getData(DataKeys.CALENDAR_POPUP_PROVIDER_FORM);
    }

    @Nullable
    CalendarTableModel getCalendarTableModel(@NotNull AnActionEvent e) {
        CalendarPopupProviderForm form = getCalendarForm(e);
        return form == null ? null : form.getCalendarTableModel();
    }
}
