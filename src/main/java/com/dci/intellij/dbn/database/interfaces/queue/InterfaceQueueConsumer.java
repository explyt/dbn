package com.dci.intellij.dbn.database.interfaces.queue;

import com.dci.intellij.dbn.common.routine.Consumer;
import com.dci.intellij.dbn.common.thread.Background;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.intellij.openapi.progress.ProgressManager;

public class InterfaceQueueConsumer implements Consumer<InterfaceTask<?>>{
    private final WeakRef<InterfaceQueue> queue;

    public InterfaceQueueConsumer(InterfaceQueue queue) {
        this.queue = WeakRef.of(queue);
    }

    @Override
    public void accept(InterfaceTask<?> task) {
        InterfaceQueue queue = getQueue();

        if (progressBackgroundSupported(task)) {
            Progress.background(queue.getProject(), queue.getConnection(), true,
                    task.getTitle(),
                    task.getText(),
                    indicator -> queue.executeTask(task));
        } else {
            Background.run(() -> queue.executeTask(task));
        }
    }

    private static boolean progressBackgroundSupported(InterfaceTask<?> task) {
        if (!task.isProgress()) return false;

        ProgressManager progressManager = ProgressManager.getInstance();
        return !progressManager.hasModalProgressIndicator() &&
                !progressManager.hasUnsafeProgressIndicator();
    }

    public InterfaceQueue getQueue() {
        return WeakRef.ensure(queue);
    }
}