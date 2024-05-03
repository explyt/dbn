package com.dbn.browser.action;

import com.dbn.common.action.ProjectActionGroup;
import com.dbn.common.icon.Icons;
import com.dbn.common.util.Actions;
import com.dbn.options.ConfigId;
import com.dbn.options.action.ProjectSettingsOpenAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

public class BrowserOptionsAction extends ProjectActionGroup {

    @NotNull
    @Override
    public AnAction[] loadChildren(AnActionEvent e) {
        return new AnAction[]{
                new AutoscrollToEditorAction(),
                new AutoscrollFromEditorAction(),
                Actions.SEPARATOR,
                new ConnectionFilterSettingsOpenAction(),
                Actions.SEPARATOR,
                new ProjectSettingsOpenAction(ConfigId.CONNECTIONS, false)
        };
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Options");
        presentation.setIcon(Icons.ACTION_OPTIONS);
    }
}
