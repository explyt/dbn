package com.dbn.navigation.psi;

import com.dbn.common.dispose.Failsafe;
import com.dbn.connection.DatabaseEntity;
import com.dbn.language.common.psi.EmptySearchScope;
import com.dbn.object.common.DBObject;
import com.dbn.object.common.list.DBObjectList;
import com.dbn.object.lookup.DBObjectRef;
import com.intellij.lang.Language;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static com.dbn.common.dispose.Failsafe.guarded;

public class DBObjectPsiDirectory implements ReadonlyPsiDirectoryStub{
    private final DBObjectRef object;

    public DBObjectPsiDirectory(@NotNull DBObjectRef object) {
        this.object = object;
    }

    @NotNull
    public DBObject getObject() {
        DBObject object = this.object.get();
        return Failsafe.nn(object);
    }

    /*********************************************************
     *                      PsiElement                       *
     *********************************************************/
    @Override
    @NotNull
    public String getName() {
        return object.getObjectName();
    }

    @Override
    public ItemPresentation getPresentation() {
        return getObject();
    }

    @Override
    @NotNull
    public Project getProject() throws PsiInvalidElementAccessException {
        return getObject().getProject();
    }

    @Override
    @NotNull
    public Language getLanguage() {
        return Language.ANY;
    }

    @Override
    public PsiDirectory getParent() {
        return guarded(null, this, e -> {
            DBObject object = e.getObject();
            DatabaseEntity parent = object.getParent();
            if (parent instanceof DBObjectList) {
                DBObjectList objectList = (DBObjectList) parent;
                return objectList.getPsiDirectory();
            }

            return null;
        });
    }

    @Override
    public void navigate(boolean requestFocus) {
        DBObject object = getObject();
        object.navigate(requestFocus);
    }

    @Override
    @NotNull
    public PsiElement[] getChildren() {
        List<PsiElement> children = new ArrayList<>();
        getObject().visitChildObjects(o -> children.add(o.getPsiDirectory()), false);
        return children.toArray(new PsiElement[0]);
    }

    @Override
    public Icon getIcon(int flags) {
        return getObject().getIcon();
    }

    /*********************************************************
     *                        PsiDirectory                   *
     *********************************************************/
    @Override
    @NotNull
    public VirtualFile getVirtualFile() {
        return getObject().getVirtualFile();
    }
}
