package com.dbn.common.ui.misc;

import com.dbn.common.action.BackgroundUpdatedAction;
import com.intellij.openapi.actionSystem.ex.CheckboxAction;
import com.intellij.openapi.project.DumbAware;

import javax.swing.*;

public abstract class DBNCheckboxAction extends CheckboxAction implements BackgroundUpdatedAction, DumbAware {
    protected DBNCheckboxAction() {
    }

    protected DBNCheckboxAction(String text) {
        super(text);
    }

    protected DBNCheckboxAction(String text, String description, Icon icon) {
        super(text, description, icon);
    }
}
