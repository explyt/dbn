package com.dbn.browser.action;

import com.dbn.browser.DatabaseBrowserManager;
import com.dbn.browser.options.BrowserDisplayMode;
import com.dbn.common.action.Lookups;
import com.dbn.common.action.ToggleAction;
import com.intellij.ide.IdeEventQueue;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BrowserDisplayModeAction extends ToggleAction {
    private final BrowserDisplayMode displayMode;

    protected BrowserDisplayModeAction(BrowserDisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    private static DatabaseBrowserManager getBrowserManager(@NotNull AnActionEvent e) {
        Project project = Lookups.ensureProject(e);

        return DatabaseBrowserManager.getInstance(project);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        DatabaseBrowserManager browserManager = getBrowserManager(e);
        BrowserDisplayMode displayMode = browserManager.getSettings().getGeneralSettings().getDisplayMode();
        return this.displayMode == displayMode;

    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        if (!state) return;

        DatabaseBrowserManager browserManager = getBrowserManager(e);
        browserManager.changeDisplayMode(displayMode);
        IdeEventQueue.getInstance().getPopupManager().closeAllPopups();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        Presentation presentation = e.getPresentation();
        presentation.setText(this.displayMode.getName());
    }

    public static class Simple extends BrowserDisplayModeAction {
        protected Simple() {
            super(BrowserDisplayMode.SIMPLE);
        }
    }

    public static class Tabbed extends BrowserDisplayModeAction {
        protected Tabbed() {
            super(BrowserDisplayMode.TABBED);
        }
    }

    public static class Selector extends BrowserDisplayModeAction {
        protected Selector() {
            super(BrowserDisplayMode.SELECTOR);
        }
    }
}
