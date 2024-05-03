package com.dbn.connection.config.action;

import com.dbn.common.action.ContextAction;
import com.dbn.common.action.DataKeys;
import com.dbn.connection.config.ui.ConnectionBundleSettingsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ConnectionSettingsAction extends ContextAction<ConnectionBundleSettingsForm> {
    @Nullable
    protected ConnectionBundleSettingsForm getTarget(@NotNull AnActionEvent e) {
        return e.getData((DataKeys.CONNECTION_BUNDLE_SETTINGS));
    }
}
