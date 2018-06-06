package com.dci.intellij.dbn.connection.transaction;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.intellij.notification.NotificationType;

import java.io.Serializable;
import java.sql.SQLException;

public enum TransactionAction implements Serializable {
    COMMIT(
            "Transaction",
            NotificationType.INFORMATION, "Connection \"{0}\" committed",
            NotificationType.ERROR, "Error committing connection \"{0}\". Details: {1}",
            false,
            new Executor() {
                void execute(ConnectionHandler connectionHandler, DBNConnection connection) throws SQLException {
                    ConnectionUtil.commit(connection);
                }
            }),

    ROLLBACK(
            "Transaction",
            NotificationType.INFORMATION, "Connection \"{0}\" rolled back.",
            NotificationType.ERROR, "Error rolling back connection \"{0}\". Details: {1}",
            false,
            new Executor() {
                void execute(ConnectionHandler connectionHandler, DBNConnection connection) throws SQLException {
                    ConnectionUtil.rollback(connection);
                }
            }),

    ROLLBACK_IDLE(
            "Transaction",
            NotificationType.INFORMATION, "Connection \"{0}\" rolled back.",
            NotificationType.ERROR, "Error rolling back connection \"{0}\". Details: {1}",
            false,
            new Executor() {
                void execute(ConnectionHandler connectionHandler, DBNConnection connection) throws SQLException {
                    ConnectionUtil.rollback(connection);
                }
            }),

    DISCONNECT(
            "Session",
            NotificationType.INFORMATION, "Disconnected from \"{0}\"",
            NotificationType.WARNING, "Error disconnecting from \"{0}\". Details: {1}",
            true,
            new Executor() {
                @Override
                void execute(ConnectionHandler connectionHandler, DBNConnection connection) throws SQLException {
                    connectionHandler.disconnect();
                }
            }),

    DISCONNECT_IDLE(
            "Session",
            NotificationType.INFORMATION, "Disconnected from \"{0}\" because it has exceeded the configured idle timeout.",
            NotificationType.WARNING, "Error disconnecting from \"{0}\". Details: {1}",
            true,
            new Executor() {
                void execute(ConnectionHandler connectionHandler, DBNConnection connection) throws SQLException {
                    connectionHandler.disconnect();
                }
            }),

    KEEP_ALIVE(
            "Ping",
            null, "",
            NotificationType.ERROR, "Error checking connectivity for \"{0}\". Details: {1}",
            false,
            new Executor() {
                @Override
                void execute(ConnectionHandler connectionHandler, DBNConnection connection) throws SQLException {
                    connection.updateLastAccess();
                }
            }),

    TURN_AUTO_COMMIT_ON(
            "Transaction",
            NotificationType.WARNING,
            "Auto-Commit switched ON for connection \"{0}\".",
            NotificationType.ERROR, "Error switching Auto-Commit ON for connection \"{0}\". Details: {1}",
            true,
            new Executor() {
                void execute(ConnectionHandler connectionHandler, DBNConnection connection) throws SQLException {
                    connectionHandler.setAutoCommit(true);
                }
            }),

    TURN_AUTO_COMMIT_OFF(
            "Transaction",
            NotificationType.INFORMATION, "Auto-Commit switched OFF for connection \"{0}\".",
            NotificationType.ERROR, "Error switching Auto-Commit OFF for connection\"{0}\". Details: {1}",
            true,
            new Executor() {
                void execute(ConnectionHandler connectionHandler, DBNConnection connection) throws SQLException {
                    connectionHandler.setAutoCommit(false);
                }
            });


    private String name;
    private String successNotificationMessage;
    private String failureNotificationMessage;
    private NotificationType notificationType;
    private NotificationType failureNotificationType;
    private Executor executor;
    private boolean isStatusChange;

    TransactionAction(String name, NotificationType notificationType, String successNotificationMessage, NotificationType failureNotificationType, String failureNotificationMessage, boolean isStatusChange, Executor executor) {
        this.name = name;
        this.failureNotificationMessage = failureNotificationMessage;
        this.successNotificationMessage = successNotificationMessage;
        this.executor = executor;
        this.isStatusChange = isStatusChange;
        this.notificationType = notificationType;
        this.failureNotificationType = failureNotificationType;
    }

    public String getName() {
        return name;
    }

    public String getSuccessNotificationMessage() {
        return successNotificationMessage;
    }

    public String getFailureNotificationMessage() {
        return failureNotificationMessage;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public NotificationType getFailureNotificationType() {
        return failureNotificationType;
    }

    public boolean isStatusChange() {
        return isStatusChange;
    }

    private abstract static class Executor {
        abstract void execute(ConnectionHandler connectionHandler, DBNConnection connection) throws SQLException;
    }

    public void execute(ConnectionHandler connectionHandler, DBNConnection connection) throws SQLException {
        executor.execute(connectionHandler, connection);
    }

}
