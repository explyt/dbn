package com.dbn.diagnostics;

import com.dbn.common.icon.IconBundle;
import com.dbn.common.icon.Icons;
import com.dbn.common.ui.window.DBNToolWindowFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;

public class DiagnosticsToolWindowFactory extends DBNToolWindowFactory {
    @Override
    public void createContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        toolWindow.setTitle("DB Diagnostics");
        toolWindow.setStripeTitle("DB Diagnostics");
        toolWindow.setToHideOnEmptyContent(true);
        toolWindow.setAutoHide(false);
        toolWindow.setAvailable(false, null);
    }

    @Override
    protected IconBundle createIconBundle() {
        return new IconBundle(
                Icons.WINDOW_DATABASE_DIAGNOSTICS,
                Icons.WINDOW_DATABASE_DIAGNOSTICS_S,
                Icons.WINDOW_DATABASE_DIAGNOSTICS_L,
                Icons.WINDOW_DATABASE_DIAGNOSTICS_LS);
    }


    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return false;
    }
}
