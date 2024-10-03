package com.dbn.database.interfaces.queue;

import com.dbn.common.ref.WeakRef;
import com.dbn.common.routine.Consumer;
import com.dbn.common.thread.Background;
import com.dbn.common.thread.Progress;
import com.dbn.common.thread.ThreadMonitor;
import com.dbn.common.thread.ThreadProperty;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;

import java.awt.*;
import java.util.Arrays;

public class InterfaceQueueConsumer implements Consumer<InterfaceTask<?>>{
    private final WeakRef<InterfaceQueue> queue;

    public InterfaceQueueConsumer(InterfaceQueue queue) {
        this.queue = WeakRef.of(queue);
    }

    @Override
    public void accept(InterfaceTask<?> task) {
        ThreadMonitor.surround(
                getProject(),
                ThreadProperty.DATABASE_INTERFACE,
                () -> schedule(task, getQueue()));
    }

    private static void schedule(InterfaceTask<?> task, InterfaceQueue queue) {
        Project project = queue.getProject();
        if (useProgress(task)) {
            InterfaceThreadMonitor.start(true);
            Progress.background(project, queue.getConnection(), true,
                    task.getTitle(),
                    task.getText(),
                    indicator -> executeTask(task, queue));
        } else {
            InterfaceThreadMonitor.start(false);
            Background.run(project, () -> executeTask(task, queue));
        }
    }

    private static void executeTask(InterfaceTask<?> task, InterfaceQueue queue) {
        try {
            queue.executeTask(task);
        } finally {
            boolean progress = ThreadMonitor.isProgressProcess();
            InterfaceThreadMonitor.finish(progress);
        }
    }

    private static boolean canUseProgress(InterfaceTask<?> task) {
        if (!task.isProgress()) return false;
        if (isModalDialogOpen()) return false;
        if (isProgressModalOrExhausted()) return false;

        return true;
    }

    private static boolean isModalDialogOpen() {
        // BACKGROUND:
        // progress tasks do not start when application is blocked by a modal dialog (preventing database interaction from modal dialogs)
        // TODO check if there is a way to circumvent this (currently routing to Background thread pool)
        return Arrays.stream(Window.getWindows()).filter(w -> w  instanceof Dialog).map(w -> (Dialog) w).anyMatch(d -> d.isModal() && d.isShowing());
    }

    private static boolean isProgressModalOrExhausted() {
        ProgressManager progressManager = ProgressManager.getInstance();
        return progressManager.hasModalProgressIndicator() || InterfaceThreadMonitor.getRunningThreadCount(true) >= 10;
    }

    private static boolean useProgress(InterfaceTask<?> task) {
        if (canUseProgress(task)) {
            synchronized (InterfaceQueueConsumer.class) {
                return canUseProgress(task);
            }
        }
        return false;
    }

    public InterfaceQueue getQueue() {
        return WeakRef.ensure(queue);
    }

    private Project getProject() {
        return getQueue().getProject();
    }
}
