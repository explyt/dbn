/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * This software is dual-licensed to you under the Universal Permissive License
 * (UPL) 1.0 as shown at https://oss.oracle.com/licenses/upl or Apache License
 * 2.0 as shown at http://www.apache.org/licenses/LICENSE-2.0. You may choose
 * either license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.dbn.oracleAI.editor.action;

import com.dbn.common.action.ProjectAction;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.mapping.FileConnectionContextManager;
import com.dbn.database.DatabaseFeature;
import com.dbn.oracleAI.editor.AssistantEditorAdapter;
import com.dbn.oracleAI.types.ActionAIType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * Action stub for DB-Assistant editor context menu
 *
 * @author Ayoub Aarrasse (ayoub.aarrasse@oracle.com)
 * @author Dan Cioca (dan.cioca@oracle.com)
 */
public abstract class AssistantBaseEditorAction extends ProjectAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) return;

        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (file == null) return;

        FileConnectionContextManager contextManager = FileConnectionContextManager.getInstance(project);
        ConnectionHandler connection = contextManager.getConnection(file);
        if (connection == null) return;
        if (!DatabaseFeature.AI_ASSISTANT.isSupported(connection)) return;

        String selectedText = getSelectedText(e);
        if (selectedText == null) return;

        AssistantEditorAdapter.submitQuery(project, editor, connection.getConnectionId(), selectedText, getAction());
    }

    private String getSelectedText(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) return null;

        SelectionModel selectionModel = editor.getSelectionModel();
        String text = selectionModel.getSelectedText();
        if (text == null) return null;

        text = text.trim();
        if (text.length() < 10) return null; // do not expose the actions if no text is selected

        return text;
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        String selectedText = getSelectedText(e);
        e.getPresentation().setVisible(selectedText != null);
    }

    protected abstract ActionAIType getAction();
}
