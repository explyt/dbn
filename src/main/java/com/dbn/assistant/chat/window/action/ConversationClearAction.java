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
import com.dbn.assistant.state.AssistantState;
import com.dbn.common.icon.Icons;
import com.dbn.common.util.Messages;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import static com.dbn.common.util.Conditional.when;

/**
 * Action for clearing the AI Assistant conversation history
 *
 * @author Dan Cioca (Oracle)
 */
public class ConversationClearAction extends AbstractChatBoxAction {
    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        ChatBoxForm chatBox = getChatBox(e);
        if (chatBox == null) return;

        promptConversationClearing(chatBox);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        AssistantState state = getAssistantState(e);
        boolean enabled = state != null && !state.getMessages().isEmpty();

        Presentation presentation = e.getPresentation();
        presentation.setIcon(Icons.ACTION_DELETE);
        presentation.setText("Clear Conversation");
        presentation.setDescription("Clear all conversation messages");
        presentation.setEnabled(enabled);
    }

    public void promptConversationClearing(ChatBoxForm chatBox) {
        Messages.showQuestionDialog(getProject(), "Conversation Clearing", "Are you sure you want to clear this conversation?",
            Messages.OPTIONS_YES_NO, 1,
            option -> when(option == 0, chatBox::clearConversation));
    }

}