package com.dbn.navigation;

import com.dbn.language.common.DBLanguagePsiFile;
import com.dbn.navigation.psi.DBConnectionPsiDirectory;
import com.dbn.navigation.psi.DBObjectPsiFile;
import com.dbn.navigation.psi.DBObjectPsiDirectory;
import com.dbn.object.common.DBObject;
import com.dbn.object.common.DBObjectPsiCache;
import com.dbn.vfs.DBVirtualFileBase;
import com.intellij.ide.navigationToolbar.AbstractNavBarModelExtension;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

import static com.dbn.common.dispose.Checks.invalidToNull;
import static com.dbn.common.dispose.Checks.isValid;
import static com.dbn.common.dispose.Failsafe.guarded;

public class NavigationBarExtension extends AbstractNavBarModelExtension {
    @Override
    public String getPresentableText(Object object) {
        if (object instanceof DBObject) {
            DBObject dbObject = (DBObject) object;
            return dbObject.getName();
        }
        return null;
    }

    @Override
    public PsiElement getParent(PsiElement psiElement) {
        return invalidToNull(guarded(null, psiElement, e -> parent(e)));
    }

    private static @Nullable PsiElement parent(PsiElement psiElement) {
        if (psiElement instanceof DBObjectPsiFile ||
                psiElement instanceof DBObjectPsiDirectory ||
                psiElement instanceof DBConnectionPsiDirectory) {

            return psiElement.getParent();
        }
        return null;
    }

    @Override
    public PsiElement adjustElement(@NotNull PsiElement psiElement) {
        return invalidToNull(guarded(null, psiElement, e -> adjusted(e)));
    }

    private static PsiElement adjusted(@NotNull PsiElement psiElement) {
        if (psiElement instanceof DBLanguagePsiFile) {
            DBLanguagePsiFile databaseFile = (DBLanguagePsiFile) psiElement;
            VirtualFile virtualFile = databaseFile.getVirtualFile();
            if (virtualFile instanceof DBVirtualFileBase) {
                DBObject object = databaseFile.getUnderlyingObject();
                return DBObjectPsiCache.asPsiFile(object);
            }
        }
        return psiElement;
    }

    @NotNull
    @Override
    public Collection<VirtualFile> additionalRoots(Project project) {
        return Collections.emptyList();
    }

    @Override
    public boolean processChildren(Object object, Object rootElement, Processor<Object> processor) {
        return true;

    }
}
