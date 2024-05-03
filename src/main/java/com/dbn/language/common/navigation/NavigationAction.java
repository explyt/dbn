package com.dbn.language.common.navigation;

import com.dbn.common.action.BasicAction;
import com.dbn.editor.code.SourceCodeManager;
import com.dbn.language.common.PsiElementRef;
import com.dbn.language.common.psi.BasePsiElement;
import com.dbn.object.common.DBObject;
import com.dbn.object.common.DBSchemaObject;
import com.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class NavigationAction extends BasicAction {
    private final PsiElementRef<BasePsiElement> navigationElement;
    private final DBObjectRef<DBObject> parentObjectRef;

    NavigationAction(String text, Icon icon, @Nullable DBObject parentObject, @NotNull BasePsiElement navigationElement) {
        super(text, null, icon);
        this.parentObjectRef = DBObjectRef.of(parentObject);
        this.navigationElement = PsiElementRef.of(navigationElement);
    }

    public DBObject getParentObject() {
        return DBObjectRef.get(parentObjectRef);
    }

    public BasePsiElement getNavigationElement() {
        return PsiElementRef.get(navigationElement);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        BasePsiElement navigationElement = getNavigationElement();
        DBObject parentObject = getParentObject();
        if (parentObject != null) {
            SourceCodeManager codeEditorManager = SourceCodeManager.getInstance(parentObject.getProject());
            codeEditorManager.navigateToObject((DBSchemaObject) parentObject, navigationElement);
        } else {
            navigationElement.navigate(true);
        }
    }
}
