package com.dbn.execution.common.ui;

import com.dbn.common.action.BasicAction;
import com.dbn.common.ref.WeakRef;
import com.dbn.common.util.Dialogs;
import com.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.dbn.execution.statement.result.ui.RenameExecutionResultDialog;
import com.dbn.execution.statement.result.ui.StatementExecutionResultForm;
import com.intellij.openapi.actionSystem.*;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.TabLabel;
import org.jetbrains.annotations.NotNull;

public class ExecutionConsolePopupActionGroup extends DefaultActionGroup {
    private final WeakRef<ExecutionConsoleForm> executionConsoleForm;

    public ExecutionConsolePopupActionGroup(ExecutionConsoleForm executionConsoleForm) {
        this.executionConsoleForm = WeakRef.of(executionConsoleForm);
        add(rename);
        addSeparator();
        add(close);
        add(closeAll);
        add(closeAllButThis);
    }

    public ExecutionConsoleForm getExecutionConsoleForm() {
        return executionConsoleForm.ensure();
    }

    private static TabInfo getTabInfo(AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        Object o = dataContext.getData(PlatformDataKeys.CONTEXT_COMPONENT.getName());
        if (o instanceof TabLabel) {
            TabLabel tabLabel = (TabLabel) o;
            return tabLabel.getInfo();
        }
        return null;
    }

    private final AnAction rename = new BasicAction("Rename Result...") {
        @Override
        public void update(@NotNull AnActionEvent e) {
            TabInfo tabInfo = getTabInfo(e);
            boolean visible = false;
            if (tabInfo != null) {
                Object object = tabInfo.getObject();
                visible = object instanceof StatementExecutionResultForm;
            }
            e.getPresentation().setVisible(visible);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            TabInfo tabInfo = getTabInfo(e);
            if (tabInfo == null) return;

            Object object = tabInfo.getObject();
            if (!(object instanceof StatementExecutionResultForm)) return;

            StatementExecutionResultForm resultForm = (StatementExecutionResultForm) object;
            StatementExecutionCursorResult executionResult = resultForm.getExecutionResult();
            Dialogs.show(() -> new RenameExecutionResultDialog(executionResult), (dialog, exitCode) -> tabInfo.setText(executionResult.getName()));
        }
    };

    private final AnAction close = new BasicAction("Close") {
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            TabInfo tabInfo = getTabInfo(e);
            if (tabInfo != null) {
                getExecutionConsoleForm().removeTab(tabInfo);
            }
        }
    };

    private final AnAction closeAll = new BasicAction("Close All") {
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            getExecutionConsoleForm().removeAllTabs();
        }
    };

    private final AnAction closeAllButThis = new BasicAction("Close All But This") {
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            TabInfo tabInfo = getTabInfo(e);
            if (tabInfo != null) {
                getExecutionConsoleForm().removeAllExceptTab(tabInfo);
            }
        }
    };
}
