package com.dbn.common.notification;

import com.dbn.common.util.Titles;
import com.dbn.common.project.ProjectSupplier;
import com.dbn.nls.NlsSupport;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

public interface NotificationSupport extends ProjectSupplier, NlsSupport {

    default void sendNotification(NotificationType type, NotificationGroup group, String message) {
        sendNotification(getProject(), type, group, message);
    }

    default void sendInfoNotification(NotificationGroup group, String message) {
        sendInfoNotification(getProject(), group, message);
    }

    default void sendWarningNotification(NotificationGroup group, String message) {
        sendWarningNotification(getProject(), group, message);
    }

    default void sendErrorNotification(NotificationGroup group, String message) {
        sendErrorNotification(getProject(), group, message);
    }


    static void sendInfoNotification(@Nullable Project project, NotificationGroup group, String message) {
        sendNotification(project, NotificationType.INFORMATION, group, message);
    }

    static void sendWarningNotification(@Nullable Project project, NotificationGroup group, String message) {
        sendNotification(project, NotificationType.WARNING, group, message);
    }

    static void sendErrorNotification(@Nullable Project project, NotificationGroup area, String message) {
        sendNotification(project, NotificationType.ERROR, area, message);
    }

    static void sendNotification(@Nullable Project project, NotificationType type, NotificationGroup group, String message) {
        if (project != null && project.isDisposed()) return;

        Notification notification = new Notification(
                "DBNavigator.NotificationGroup",
                Titles.signed(group.toString()), // TODO NLS
                message,
                type);
        notification.setImportant(false);
        Notifications.Bus.notify(notification, project);
    }
}
