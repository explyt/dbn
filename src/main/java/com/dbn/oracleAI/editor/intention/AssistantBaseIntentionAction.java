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

package com.dbn.oracleAI.editor.intention;

import com.dbn.code.common.intention.GenericIntentionAction;
import com.dbn.common.util.Documents;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.mapping.FileConnectionContextManager;
import com.dbn.oracleAI.editor.AssistantEditorAdapter;
import com.dbn.oracleAI.types.ActionAIType;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.dbn.common.dispose.Checks.isValid;
import static com.dbn.database.DatabaseFeature.AI_ASSISTANT;

/**
 * Intention action stub for DB-Assistant editor intentions
 *
 * @author Ayoub Aarrasse (ayoub.aarrasse@oracle.com)
 * @author Dan Cioca (dan.cioca@oracle.com)
 */
public abstract class AssistantBaseIntentionAction extends GenericIntentionAction {
    @Override
    public final boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return isDatabaseAssistantPrompt(element) && isValid(editor) && isSupported(project, editor);
    }

    @Override
    public final void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        ConnectionHandler connection = getConnection(project, editor);
        if (connection == null) return;
        if (!AI_ASSISTANT.isSupported(connection)) return;

        String prompt = element.getText();
        AssistantEditorAdapter.submitQuery(project, editor, connection.getConnectionId(), prompt, getAction());
    }

    protected abstract ActionAIType getAction();

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    private static @Nullable ConnectionHandler getConnection(@NotNull Project project, Editor editor) {
        VirtualFile file = Documents.getVirtualFile(editor);
        if (file == null) return null;

        FileConnectionContextManager contextManager = FileConnectionContextManager.getInstance(project);
        return contextManager.getConnection(file);
    }

    protected boolean isSupported(@NotNull Project project, Editor editor) {
        ConnectionHandler connection = getConnection(project, editor);
        return AI_ASSISTANT.isSupported(connection);
    }

    @Override
    public Icon getIcon(int flags) {
        return null;
    }
}
