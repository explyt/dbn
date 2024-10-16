package com.dbn.common.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.ActionUpdateThreadAware;
import org.jetbrains.annotations.NotNull;

public interface BackgroundUpdatedAction extends ActionUpdateThreadAware {

    @Override
    default @NotNull ActionUpdateThread getActionUpdateThread() {
        return isUpdateInBackground() ? ActionUpdateThread.BGT : ActionUpdateThread.EDT;
    }

    default boolean isUpdateInBackground() {
        return true;
    }

}
