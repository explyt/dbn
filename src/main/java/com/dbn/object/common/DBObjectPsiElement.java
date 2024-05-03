package com.dbn.object.common;

import com.dbn.common.dispose.Checks;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.SchemaId;
import com.dbn.connection.context.DatabaseContextBase;
import com.dbn.language.sql.SQLLanguage;
import com.dbn.navigation.psi.ReadonlyPsiElementStub;
import com.dbn.object.lookup.DBObjectRef;
import com.dbn.object.type.DBObjectType;
import com.intellij.lang.Language;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DBObjectPsiElement implements PsiNamedElement, ReadonlyPsiElementStub, NavigationItem, DatabaseContextBase {
    private final DBObjectRef<?> object;

    public DBObjectPsiElement(DBObjectRef<?> object) {
        this.object = object;
    }

    @Nullable
    @Override
    public String getName() {
        return object.getObjectName();
    }

    @Nullable
    @Override
    public ConnectionHandler getConnection() {
        return object.getConnection();
    }

    @Nullable
    @Override
    public SchemaId getSchemaId() {
        return object.getSchemaId();
    }

    @Nullable
    @Override
    public ItemPresentation getPresentation() {
        return ensureObject().getPresentation();
    }

    /*********************************************************
     *                    PsiNamedElement                    *
     *********************************************************/

    @Override
    public PsiElement getParent(){return null;}

    @Override
    public PsiFile getContainingFile() throws PsiInvalidElementAccessException {
        return ensureObject().getObjectBundle().getFakeObjectFile();
    }

    @Override
    public PsiElement copy() {return this;}

    @Override
    public boolean isValid() {
        DBObject object = getObject();
        return Checks.isValid(object) && Checks.isValid(object.getParentObject());
    }

    @NotNull
    @Override
    public Project getProject() throws PsiInvalidElementAccessException {
        return ensureObject().getProject();
    }

    @Override
    @NotNull
    public Language getLanguage() {
        return SQLLanguage.INSTANCE;
    }

    @Override
    public TextRange getTextRange() {
        return new TextRange(0, getText().length());
    }

    @Override
    public int getTextLength() {
        return getText().length();
    }

    @Override
    @NonNls
    public String getText() {
        return getName();
    }

    @Override
    public char[] textToCharArray() {
        return getText().toCharArray();
    }

    @Override
    public void navigate(boolean requestFocus) {
        ensureObject().navigate(requestFocus);
    }

    @Override
    public Icon getIcon(int flags) {
        return ensureObject().getIcon();
    }

    @NotNull
    public DBObject ensureObject() {
        return DBObjectRef.ensure(object);
    }

    @Nullable
    public DBObject getObject() {
        return DBObjectRef.get(object);
    }

    @NotNull
    public DBObjectType getObjectType() {
        return object.getObjectType();
    }
}
