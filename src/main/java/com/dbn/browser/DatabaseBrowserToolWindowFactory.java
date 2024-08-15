package com.dbn.browser;

import com.dbn.browser.ui.BrowserToolWindowForm;
import com.dbn.common.ui.window.DBNToolWindowFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

public class DatabaseBrowserToolWindowFactory extends DBNToolWindowFactory {

    @Override
    public void createContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
        BrowserToolWindowForm toolWindowForm = browserManager.getToolWindowForm();

        ContentManager contentManager = toolWindow.getContentManager();
        ContentFactory contentFactory = contentManager.getFactory();
        Content content = contentFactory.createContent(toolWindowForm.getComponent(), null, true);

        toolWindow.setTitle("DB Browser");
        toolWindow.setStripeTitle("DB Browser");
        contentManager.addContent(content);
    }
}
