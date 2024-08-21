package com.dbn.oracleAI.action;

import com.dbn.common.action.ProjectAction;
import com.dbn.oracleAI.AIProfileItem;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ProfileSelectAction extends ProjectAction {
    private final AIProfileItem profile;
    ProfileSelectAction(AIProfileItem session) {
        this.profile = session;
    }


    @NotNull
    public AIProfileItem getProfile() {
        return profile;
    }


    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {

    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        Presentation presentation = e.getPresentation();
        presentation.setText(profile.getLabel());
    }
}
