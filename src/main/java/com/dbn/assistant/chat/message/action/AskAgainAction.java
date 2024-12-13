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

package com.dbn.assistant.chat.message.action;

import com.dbn.assistant.chat.window.ui.ChatBoxForm;
import com.dbn.common.action.BasicAction;
import com.dbn.common.action.DataKeys;
import com.dbn.common.icon.Icons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

public class AskAgainAction extends BasicAction {
  private final String content;

  public AskAgainAction(String content) {
    this.content = content;
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    Presentation presentation = e.getPresentation();
    presentation.setText(txt("app.assistant.action.AskAgain"));
    presentation.setDescription(txt("app.assistant.action.AskAgainDesc"));
    presentation.setIcon(Icons.ACTION_RETRY);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
    ChatBoxForm chatBox = anActionEvent.getData(DataKeys.ASSISTANT_CHAT_BOX);
    assert chatBox != null;
    chatBox.submitPrompt(content);
  }

}
