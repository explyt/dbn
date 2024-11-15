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

package com.dbn.assistant.editor.intention;

import com.dbn.assistant.DatabaseAssistantManager;
import com.dbn.assistant.editor.AssistantEditorUtil;
import com.dbn.code.common.intention.EditorIntentionAction;
import com.dbn.code.common.intention.EditorIntentionType;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionId;
import com.dbn.object.DBAIProfile;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.dbn.assistant.editor.AssistantEditorUtil.isAssistantAvailable;
import static com.dbn.assistant.editor.AssistantPromptUtil.isAssistantPromptAvailable;
import static com.dbn.common.dispose.Checks.isNotValid;

/**
 * Editor intention action allowing to switch the current profile
 *
 * @author Dan Cioca(Oracle)
 */
public class ProfileSwitchIntentionAction extends EditorIntentionAction {
  @Override
  public EditorIntentionType getType() {
    return EditorIntentionType.ASSISTANT_PROFILE_SELECT;
  }

  public final String getText() {
    return "Select AI - Switch Profile";
  }

  @Override
  public final boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
    return isAssistantAvailable(editor) &&
            getProfiles(editor).size() > 1 &&
            isAssistantPromptAvailable(editor, element);
  }

  @Override
  public final void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
    ConnectionHandler connection = AssistantEditorUtil.getConnection(editor);
    if (isNotValid(connection)) return;

    DatabaseAssistantManager manager = DatabaseAssistantManager.getInstance(project);
    ConnectionId connectionId = connection.getConnectionId();
    manager.promptProfileSelector(editor, connectionId);
  }

  private static List<DBAIProfile> getProfiles(Editor editor) {
    ConnectionHandler connection = AssistantEditorUtil.getConnection(editor);
    if (isNotValid(connection)) return Collections.emptyList();

    Project project = connection.getProject();

    DatabaseAssistantManager manager = DatabaseAssistantManager.getInstance(project);
    return manager.getProfiles(connection.getConnectionId());
  }

}
