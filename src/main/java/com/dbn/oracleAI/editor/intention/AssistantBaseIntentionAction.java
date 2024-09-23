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
import com.dbn.connection.ConnectionHandler;
import com.dbn.oracleAI.editor.AssistantEditorActionUtil;
import com.dbn.oracleAI.editor.AssistantEditorAdapter;
import com.dbn.oracleAI.types.ActionAIType;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.dbn.common.dispose.Checks.isNotValid;
import static com.dbn.oracleAI.editor.AssistantEditorActionUtil.resolvePromptText;

/**
 * Intention action stub for DB-Assistant editor intentions
 *
 * @author Ayoub Aarrasse (Oracle)
 * @author Dan Cioca (Oracle)
 */
public abstract class AssistantBaseIntentionAction extends GenericIntentionAction {
    @Override
    public final boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return AssistantEditorActionUtil.isAssistantSupported(editor) && resolvePromptText(editor, element) != null;
    }

    @Override
    public final void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        ConnectionHandler connection = AssistantEditorActionUtil.getConnection(editor);
        if (isNotValid(connection)) return;

        String promptText = resolvePromptText(editor, element);
        if (promptText == null) return;

        AssistantEditorAdapter.submitQuery(project, editor, connection.getConnectionId(), promptText, getAction());
    }

    protected abstract ActionAIType getAction();

    protected abstract String getActionName();

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    public Icon getIcon(int flags) {
        return null;
    }

    @NotNull
    @Override
    public final String getText() {
        return "Select AI - " + getActionName();
    }
}
