package com.dci.intellij.dbn.common.environment.options.listener;

import com.dci.intellij.dbn.vfs.file.DBContentVirtualFile;
import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface EnvironmentManagerListener extends EventListener {
    Topic<EnvironmentManagerListener> TOPIC = Topic.create("Environment changed", EnvironmentManagerListener.class);
    void configurationChanged();
    void editModeChanged(DBContentVirtualFile databaseContentFile);
}
