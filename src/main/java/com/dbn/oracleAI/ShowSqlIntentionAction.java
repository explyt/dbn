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

package com.dbn.oracleAI;

import com.dbn.common.util.Documents;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.mapping.FileConnectionContextManager;
import com.dbn.database.DatabaseFeature;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInsight.intention.impl.config.IntentionManagerImpl;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * Editor intention action for invoking AI-Assistant module from within the editor
 *
 * @author Ayoub Aarrasse (ayoub.aarrasse@oracle.com)
 */
public class ShowSqlIntentionAction extends PsiElementBaseIntentionAction {

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
    if (element instanceof PsiComment) {
      String text = element.getText();
      return text.startsWith("---") && (text.endsWith(";") || text.endsWith(";\n"));
    }
    return false;
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
    String line = element.getText();

    VirtualFile file = Documents.getVirtualFile(editor);
    if (file == null) return;

    FileConnectionContextManager contextManager = FileConnectionContextManager.getInstance(project);
    ConnectionHandler connection = contextManager.getConnection(file);
    if (connection == null) return;
    if (!DatabaseFeature.AI_ASSISTANT.isSupported(connection)) return;

    new ShowSqlOnEditor(project).processQuery(connection.getConnectionId(), line, editor.getDocument(), true);
  }

  @NotNull
  @Override
  public String getFamilyName() {
    return getText();
  }

  public static void register() {
    IntentionManagerImpl.getInstance().addAction(new ShowSqlIntentionAction());
  }

  @Override
  @NotNull
  public String getText() {
    return "Show SQL Query";
  }

  @Override
  public boolean startInWriteAction() {
    return false;
  }

}
