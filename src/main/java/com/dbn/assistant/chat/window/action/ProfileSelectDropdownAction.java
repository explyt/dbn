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

package com.dbn.assistant.chat.window.action;

import com.dbn.assistant.chat.window.ui.ChatBoxForm;
import com.dbn.common.action.ComboBoxAction;
import com.dbn.common.action.DataKeys;
import com.dbn.common.util.Actions;
import com.dbn.object.DBAIProfile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import java.util.List;

/**
 * Action for selecting the current AI-assistant profile
 *
 * @author Dan Cioca (Oracle)
 */
public class ProfileSelectDropdownAction extends ComboBoxAction implements DumbAware {

    @Override
    @NotNull
    protected DefaultActionGroup createPopupActionGroup(JComponent component, DataContext dataContext) {
        DefaultActionGroup actionGroup = new DefaultActionGroup();

        ChatBoxForm chatBox = dataContext.getData(DataKeys.ASSISTANT_CHAT_BOX);
        if (chatBox == null) return actionGroup;

        List<DBAIProfile> profiles = chatBox.getProfiles();
        profiles.forEach(p -> actionGroup.add(new ProfileSelectAction(p)));
        actionGroup.addSeparator();

        actionGroup.add(new ProfileCreateAction());
        return actionGroup;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        ChatBoxForm chatBox = e.getData(DataKeys.ASSISTANT_CHAT_BOX);
        boolean enabled = chatBox != null && chatBox.getAssistantState().isAvailable();

        DBAIProfile profile = getSelectedProfile(e);

        Presentation presentation = e.getPresentation();
        presentation.setText(getText(e));
        presentation.setDescription(txt("companion.chat.profile.tooltip"));
        presentation.setEnabled(enabled);
        presentation.setIcon(profile == null ? null : profile.getIcon());
    }

    private String getText(@NotNull AnActionEvent e) {
        ChatBoxForm chatBox = e.getData(DataKeys.ASSISTANT_CHAT_BOX);
        if (chatBox == null) return "Profile";

        String text = getSelectedProfileName(e);
        if (text != null) return text;

        List<DBAIProfile> profiles = chatBox.getProfiles();
        if (!profiles.isEmpty()) return "Select Profile";

        return "Profile";
    }

    @Nullable
    private static String getSelectedProfileName(@NotNull AnActionEvent e) {
        DBAIProfile profile = getSelectedProfile(e);
        if (profile == null) return null;

        return Actions.adjustActionName(profile.getName());
    }

    @Nullable
    private static DBAIProfile getSelectedProfile(@NotNull AnActionEvent e) {
        ChatBoxForm chatBox = e.getData(DataKeys.ASSISTANT_CHAT_BOX);
        if (chatBox == null) return null;

        return chatBox.getSelectedProfile();
    }

    @Override
    protected boolean shouldShowDisabledActions() {
        return true;
    }
}
