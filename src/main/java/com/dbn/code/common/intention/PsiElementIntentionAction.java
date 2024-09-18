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

package com.dbn.code.common.intention;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Alternative implementation of {@link com.intellij.codeInsight.intention.PsiElementBaseIntentionAction}
 * Fixes element lookup when caret is at the end of file
 *
 * @author Dan Cioca (dan.cioca@oracle.com)
 */
public abstract class PsiElementIntentionAction extends BaseIntentionAction {

    public final void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        if (editor == null) return;
        if (!this.checkFile(file)) return;

        PsiElement element = getElement(editor, file);
        if (element == null) return;

        this.invoke(project, editor, element);
    }

    public boolean checkFile(@Nullable PsiFile file) {
        return file != null && canModify(file);
    }

    public abstract void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException;

    public final boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        if (editor == null) return false;
        if (!this.checkFile(file)) return false;

        PsiElement element = getElement(editor, file);
        if (element == null) return false;

        return isAvailable(project, editor, element);
    }

    public abstract boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement);

    @Nullable
    private static PsiElement getElement(@NotNull Editor editor, @NotNull PsiFile file) {
        CaretModel caretModel = editor.getCaretModel();
        int position = caretModel.getOffset();
        PsiElement psiElement = file.findElementAt(position);
        if (psiElement != null) return psiElement;

        // null element at this stage is either empty file or caret at end-of-file
        return file.getLastChild();
    }
}
