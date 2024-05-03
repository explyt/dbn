package com.dbn.object.factory.ui.common;

import com.dbn.common.action.BasicAction;
import com.dbn.common.icon.Icons;
import com.dbn.common.ui.form.DBNFormBase;
import com.dbn.common.util.Actions;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class ObjectListItemForm extends DBNFormBase {
    private JPanel mainPanel;
    private JPanel removeActionPanel;
    private JPanel objectDetailsComponent;

    private final ObjectFactoryInputForm<?> inputForm;

    ObjectListItemForm(@NotNull ObjectListForm<?> parent, ObjectFactoryInputForm<?> inputForm) {
        super(parent);
        this.inputForm = inputForm;
        ActionToolbar actionToolbar = Actions.createActionToolbar(removeActionPanel,
                "DBNavigator.ObjectFactory.AddElement", true,
                new RemoveObjectAction());
        removeActionPanel.add(actionToolbar.getComponent(), BorderLayout.NORTH);

    }

    @NotNull
    public ObjectListForm<?> getParentForm() {
        return ensureParentComponent();
    }

    @NotNull
    @Override
    public JPanel getMainComponent(){
        return mainPanel;
    }

    private void createUIComponents() {
        objectDetailsComponent = (JPanel) inputForm.getComponent();
    }

    public class RemoveObjectAction extends BasicAction {
        RemoveObjectAction() {
            super("Remove " + getParentForm().getObjectType().getName(), null, Icons.ACTION_CLOSE);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            getParentForm().removeObjectPanel(ObjectListItemForm.this);
        }
    }

    ObjectFactoryInputForm<?> getObjectDetailsPanel() {
        return inputForm;
    }
}
