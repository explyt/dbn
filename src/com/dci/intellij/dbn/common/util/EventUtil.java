package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EventUtil {
    private static MessageBusConnection connect(@Nullable Disposable parentDisposable) {
        MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();
        return parentDisposable == null ?
                messageBus.connect() :
                messageBus.connect(parentDisposable);
    }

    private static MessageBusConnection connect(Project project, @Nullable Disposable parentDisposable) {
        MessageBus messageBus = project.getMessageBus();
        return parentDisposable == null ?
                messageBus.connect(project) :
                messageBus.connect(parentDisposable);    }
    
    public static <T> void subscribe(Project project, @Nullable Disposable parentDisposable, Topic<T> topic, final T handler) {
        if (project != null && project != Failsafe.DUMMY_PROJECT) {
            final MessageBusConnection messageBusConnection = connect(project, parentDisposable);
            messageBusConnection.subscribe(topic, handler);
        }
    }

    public static <T> void subscribe(@Nullable Disposable parentDisposable, Topic<T> topic, final T handler) {
        final MessageBusConnection messageBusConnection = connect(parentDisposable == null ? ApplicationManager.getApplication() : parentDisposable);
        messageBusConnection.subscribe(topic, handler);
    }

    @NotNull
    public static <T> T notify(@Nullable Project project, Topic<T> topic) {
        if (project == null || project.isDisposed() || project == Failsafe.DUMMY_PROJECT) {
            throw AlreadyDisposedException.INSTANCE;
        }
        MessageBus messageBus = project.getMessageBus();
        return messageBus.syncPublisher(topic);
    }
}
