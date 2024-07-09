package com.dbn.language.sql.template;

import com.dbn.language.common.psi.BasePsiElement;
import com.dbn.language.common.psi.LeafPsiElement;
import com.dbn.language.common.psi.PsiUtil;
import com.dbn.language.psql.PSQLLanguage;
import com.dbn.language.sql.SQLLanguage;
import com.dbn.vfs.file.DBDatasetFilterVirtualFile;
import com.dbn.vfs.file.DBObjectFilterExpressionFile;
import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SQLTemplateContextType extends TemplateContextType {
    protected SQLTemplateContextType() {
        super("SQL", "SQL (DBN)");
    }

    @Override
    public boolean isInContext(@NotNull PsiFile file, int offset) {
        Language language = file.getLanguage();
        if (language instanceof SQLLanguage) {
            VirtualFile virtualFile = file.getOriginalFile().getVirtualFile();
            if (virtualFile instanceof DBObjectFilterExpressionFile) return false;
            if (virtualFile instanceof DBDatasetFilterVirtualFile) return false;

            LeafPsiElement leafPsiElement = PsiUtil.lookupLeafBeforeOffset(file, offset);
            if (leafPsiElement == null) return true;

            if (leafPsiElement.getLanguage() instanceof PSQLLanguage) {
                BasePsiElement scopePsiElement = leafPsiElement.getEnclosingScopeElement();
                return scopePsiElement != null && !scopePsiElement.getTextRange().contains(offset);
            }

            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public SyntaxHighlighter createHighlighter() {
        return SQLLanguage.INSTANCE.getMainLanguageDialect().getSyntaxHighlighter();
    }
}
