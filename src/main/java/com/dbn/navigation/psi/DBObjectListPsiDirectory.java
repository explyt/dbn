package com.dbn.navigation.psi;

import com.dbn.common.dispose.Disposer;
import com.dbn.common.dispose.Failsafe;
import com.dbn.common.ref.WeakRefCache;
import com.dbn.common.util.Naming;
import com.dbn.connection.DatabaseEntity;
import com.dbn.language.common.psi.EmptySearchScope;
import com.dbn.object.common.DBObject;
import com.dbn.object.common.DBObjectBundle;
import com.dbn.object.common.DBObjectPsiCache;
import com.dbn.object.common.DBSchemaObject;
import com.dbn.object.common.list.DBObjectList;
import com.dbn.vfs.file.DBObjectListVirtualFile;
import com.intellij.lang.Language;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vcs.FileStatus;
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

public class DBObjectListPsiDirectory implements ReadonlyPsiDirectoryStub  {
    private static final WeakRefCache<DBObjectList, DBObjectListPsiDirectory> psiDirectoryCache = WeakRefCache.weakKey();

    private DBObjectListVirtualFile<?> virtualFile;

    public DBObjectListPsiDirectory(DBObjectList objectList) {
        virtualFile = new DBObjectListVirtualFile<>(objectList);
    }

    public static DBObjectListPsiDirectory of(DBObjectList objectList) {
        return psiDirectoryCache.get(objectList, ol -> new DBObjectListPsiDirectory(ol));
    }

    @NotNull
    public DBObjectList getObjectList() {
        return getVirtualFile().getObjectList();
    }

    @Override
    @NotNull
    public DBObjectListVirtualFile getVirtualFile() {
        return Failsafe.nn(virtualFile);
    }

    @Override
    public void dispose() {
        Disposer.dispose(virtualFile);
        virtualFile = null;
    }

    /*********************************************************
     *                      PsiElement                       *
     *********************************************************/
    @Override
    @NotNull
    public String getName() {
        return Naming.capitalize(getObjectList().getName());
    }

    @Override
    public ItemPresentation getPresentation() {
        return getObjectList().getPresentation();
    }

    @Override
    @NotNull
    public Project getProject() throws PsiInvalidElementAccessException {
        Project project = getVirtualFile().getProject();
        return Failsafe.nn(project);
    }

    @Override
    @NotNull
    public Language getLanguage() {
        return Language.ANY;
    }

    @Override
    public PsiDirectory getParent() {
        return guarded(null, this, e -> {
            DatabaseEntity parent = e.getObjectList().getParent();
            if (parent instanceof DBObject) {
                DBObject parentObject = (DBObject) parent;
                return DBObjectPsiCache.asPsiDirectory(parentObject);
            }

            if (parent instanceof DBObjectBundle) {
                DBObjectBundle objectBundle = (DBObjectBundle) parent;
                return objectBundle.getConnection().getPsiDirectory();
            }

            return null;
        });
    }

    @Override
    public void navigate(boolean requestFocus) {
        getObjectList().navigate(requestFocus);
    }

    @Override
    @NotNull
    public PsiElement[] getChildren() {
        List<PsiElement> children = new ArrayList<>();
        for (Object obj : getObjectList().getObjects()) {
            DBObject object = (DBObject) obj;
            if (object instanceof DBSchemaObject) {
                children.add(DBObjectPsiCache.asPsiFile(object));
            } else {
                children.add(DBObjectPsiCache.asPsiDirectory(object));
            }
        }
        return children.toArray(new PsiElement[0]);
    }

    @Override
    public Icon getIcon(int flags) {
        return null;
    }
}
