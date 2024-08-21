package com.dbn.oracleAI.action;

import com.dbn.common.action.DataKeys;
import com.dbn.common.ui.misc.DBNComboBoxAction;
import com.dbn.oracleAI.AIProfileItem;
import com.dbn.oracleAI.ui.OracleAIChatBox;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

public class ProfileSelectDropdownAction extends DBNComboBoxAction implements DumbAware {
    private static final String NAME = "Session";

    @Override
    @NotNull
    protected DefaultActionGroup createPopupActionGroup(JComponent component, DataContext dataContext) {
        DefaultActionGroup actionGroup = new DefaultActionGroup();

        OracleAIChatBox chatBox = dataContext.getData(DataKeys.COMPANION_CHAT_BOX);
        if (chatBox == null) return actionGroup;

        List<AIProfileItem> profiles = chatBox.getState().getProfiles();
        profiles.forEach(p -> actionGroup.add(new ProfileSelectAction(p)));
        actionGroup.addSeparator();
        // TODO add "create profile" / "edit profiles" actions
        return actionGroup;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        String text = "Profile";
        OracleAIChatBox chatBox = e.getData(DataKeys.COMPANION_CHAT_BOX);
        if (chatBox != null) {
            AIProfileItem selectedProfile = chatBox.getState().getSelectedProfile();
            if (selectedProfile != null) text = selectedProfile.getLabel();
        }

        Presentation presentation = e.getPresentation();
        presentation.setText(text, false);
        presentation.setDescription("Select AI Profile");
    }

    @Override
    protected boolean shouldShowDisabledActions() {
        return true;
    }
}
