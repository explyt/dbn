package com.dbn.navigation.psi;

import com.dbn.common.dispose.Disposer;
import com.dbn.common.dispose.Failsafe;
import com.dbn.connection.ConnectionHandler;
import com.dbn.language.common.psi.EmptySearchScope;
import com.dbn.object.common.list.DBObjectListContainer;
import com.dbn.vfs.file.DBConnectionVirtualFile;
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

public class DBConnectionPsiDirectory implements ReadonlyPsiDirectoryStub {
    private DBConnectionVirtualFile virtualFile;

    public DBConnectionPsiDirectory(ConnectionHandler connection) {
        virtualFile = new DBConnectionVirtualFile(connection);
    }

    @Override
    @NotNull
    public DBConnectionVirtualFile getVirtualFile() {
        return Failsafe.nn(virtualFile);
    }

    @NotNull
    public ConnectionHandler getConnection() {
        return getVirtualFile().getConnection();
    }

    @Override
    @NotNull
    public String getName() {
        return getConnection().getName();
    }

    @Override
    public ItemPresentation getPresentation() {
        return getConnection().getObjectBundle();
    }

    @Override
    public void dispose() {
        Disposer.dispose(virtualFile);
        virtualFile = null;
    }

    @Override
    @NotNull
    public Project getProject() throws PsiInvalidElementAccessException {
        return Failsafe.nn(getVirtualFile().getProject());
    }

    @Override
    @NotNull
    public Language getLanguage() {
        return Language.ANY;
    }

    @Override
    @NotNull
    public PsiElement[] getChildren() {
        List<PsiElement> children = new ArrayList<>();
        DBObjectListContainer objectLists = virtualFile.getConnection().getObjectBundle().getObjectLists();
        objectLists.visit(o -> children.add(o.getPsiDirectory()), false);
        return children.toArray(new PsiElement[0]);
    }

    @Override
    public PsiDirectory getParent() {
        return null;
    }

    @Override
    public void navigate(boolean requestFocus) {
        getConnection().getObjectBundle().navigate(requestFocus);
    }

    @Override
    public Icon getIcon(int flags) {
        return getVirtualFile().getIcon();
    }
}
