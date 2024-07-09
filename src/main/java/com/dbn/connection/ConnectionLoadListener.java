package com.dbn.connection;

import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EventListener;
import java.util.Objects;

public interface ConnectionLoadListener extends EventListener {
    Topic<ConnectionLoadListener> TOPIC = Topic.create("meta-data load event", ConnectionLoadListener.class);
    void contentsLoaded(ConnectionHandler connection);

    static ConnectionLoadListener create(
            @Nullable ConnectionId connectionId,
            @NotNull Runnable runnable) {
        return connection -> {
            if (connectionId == null || Objects.equals(connection.getConnectionId(), connectionId)) {
                runnable.run();
            }
        };
    }
}
