package com.dci.intellij.dbn.language.editor.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.action.OpenSettingsAction;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.vfs.VirtualFile;
import static com.dci.intellij.dbn.common.util.ActionUtil.getVirtualFile;

public class EditorSettingsAction extends OpenSettingsAction{
    public EditorSettingsAction() {
        super(ConfigId.CODE_COMPLETION, true);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        Presentation presentation = e.getPresentation();
        VirtualFile virtualFile = getVirtualFile(e);
        presentation.setVisible(!(virtualFile instanceof DBConsoleVirtualFile));
    }
}
