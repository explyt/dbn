package com.dbn.assistant.state;

import com.dbn.connection.ConnectionId;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface AssistantStateListener extends EventListener {
    Topic<AssistantStateListener> TOPIC = Topic.create("AssistantStateListener", AssistantStateListener.class);
    void stateChanged(Project project, ConnectionId connectionId);
}
