package com.dci.intellij.dbn.object.filter.name.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.filter.name.CompoundFilterCondition;
import com.dci.intellij.dbn.object.filter.name.ConditionJoinType;
import com.dci.intellij.dbn.object.filter.name.SimpleFilterCondition;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

public class EditFilterConditionDialog extends DBNDialog {
    private EditFilterConditionForm filterConditionForm;

    public EditFilterConditionDialog(Project project, CompoundFilterCondition parentCondition, SimpleFilterCondition condition, DBObjectType objectType, EditFilterConditionForm.Operation operation) {
        super(project, getTitle(operation), true);
        filterConditionForm = new EditFilterConditionForm(this, parentCondition, condition,  objectType, operation);
        setModal(true);
        setResizable(false);
        init();
    }

    @Nullable
    private static String getTitle(EditFilterConditionForm.Operation operation) {
        return operation == EditFilterConditionForm.Operation.CREATE ? "Create filter" :
        operation == EditFilterConditionForm.Operation.EDIT ? "Edit filter condition" :
        operation == EditFilterConditionForm.Operation.JOIN ? "Join filter condition" : null;
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return filterConditionForm.getFocusComponent();
    }

    protected String getDimensionServiceKey() {
        return "DBNavigator.ObjectFilterConditionDialog";
    }

    @Nullable
    protected JComponent createCenterPanel() {
        return filterConditionForm.getComponent();
    }

    public void doOKAction() {
        super.doOKAction();
    }

    public void doCancelAction() {
        super.doCancelAction();
    }

    public SimpleFilterCondition getCondition() {
        return filterConditionForm.getCondition();
    }

    public ConditionJoinType getJoinType() {
        return filterConditionForm.getJoinType();
    }


}
