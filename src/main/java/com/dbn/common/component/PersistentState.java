package com.dbn.common.component;

import com.dbn.common.project.ProjectContext;
import com.dbn.common.thread.ThreadMonitor;
import com.dbn.common.util.Unsafe;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dbn.common.thread.ThreadProperty.COMPONENT_STATE;

public interface PersistentState extends PersistentStateComponent<Element> {
    @Nullable
    default Project getProject() {
        return null;
    }

    @Override
    @Nullable
    default Element getState() {
        return ProjectContext.surround(getProject(),
                () -> ThreadMonitor.surround(COMPONENT_STATE,
                        () -> Unsafe.warned(null,
                                () -> getComponentState())));
    }

    @Override
    default void loadState(@NotNull Element state) {
        ProjectContext.surround(getProject(),
                () -> ThreadMonitor.surround(COMPONENT_STATE,
                        () -> Unsafe.warned(
                                () -> loadComponentState(state))));
    }

    @NonNls
    Element getComponentState();

    @NonNls
    void loadComponentState(@NotNull Element state);
}
