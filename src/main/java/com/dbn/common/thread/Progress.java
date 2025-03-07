/*
 * Copyright 2024 Oracle and/or its affiliates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dbn.common.thread;

import com.dbn.common.dispose.Checks;
import com.dbn.common.dispose.Failsafe;
import com.dbn.common.load.ProgressMonitor;
import com.dbn.common.ref.WeakRef;
import com.dbn.common.ui.progress.ProgressDialogHandler;
import com.dbn.common.util.Titles;
import com.dbn.connection.context.DatabaseContext;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.Task.Backgroundable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts.ProgressText;
import com.intellij.openapi.util.NlsContexts.ProgressTitle;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

import static com.intellij.openapi.progress.PerformInBackgroundOption.ALWAYS_BACKGROUND;

@UtilityClass
public final class Progress {
    public static void background(Project project, DatabaseContext context, boolean cancellable, @ProgressTitle String title, @ProgressText String text, ProgressRunnable runnable) {
        if (Checks.isNotValid(project)) return;
        title = Titles.suffixed(title, context);

        ThreadInfo invoker = ThreadInfo.copy();
        schedule(new Backgroundable(project, title, cancellable, ALWAYS_BACKGROUND) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                execute(indicator, ThreadProperty.PROGRESS, invoker, text, runnable);
            }
        });
    }


    public static void prompt(Project project, DatabaseContext context, boolean cancellable, @ProgressTitle String title, @ProgressText String text, ProgressRunnable runnable) {
        if (Checks.isNotValid(project)) return;
        title = Titles.suffixed(title, context);

        ProgressDialogHandler handler = new ProgressDialogHandler(project, title, text);
        ThreadInfo invoker = ThreadInfo.copy();
        schedule(new Task.Backgroundable(project, title, cancellable, ALWAYS_BACKGROUND) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    handler.init(indicator);
                    execute(indicator, ThreadProperty.PROGRESS, invoker, text, runnable);
                } finally {
                    handler.release();
                }
            }
        });
        handler.trigger();
    }


    public static void modal(Project project, DatabaseContext context, boolean cancellable, @ProgressTitle String title, @ProgressText String text, ProgressRunnable runnable) {
        if (Checks.isNotValid(project)) return;
        title = Titles.suffixed(title, context);

        ThreadInfo invoker = ThreadInfo.copy();
        schedule(new Task.Modal(project, title, cancellable) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                execute(indicator, ThreadProperty.MODAL, invoker, text, runnable);
            }
        });
    }

    private static void execute(ProgressIndicator indicator, ThreadProperty threadProperty, ThreadInfo invoker, String text, ProgressRunnable runnable) {
        ThreadMonitor.surround(invoker, threadProperty, () -> Failsafe.guarded(() -> {
            indicator.setText(text);
            runnable.run(indicator);
        }));
    }

    private static void schedule(Task task) {
        Project project = task.getProject();
        if (!Checks.allValid(task, project)) return;

        ThreadInfo info = ThreadInfo.copy();
        ProgressManager progressManager = ProgressManager.getInstance();
        Dispatch.run(ModalityState.any(),
                () -> ThreadMonitor.surround(info, null,
                        () -> progressManager.run(task)));
    }

    public static double progressOf(int is, int should) {
        return ((double) is) / should;
    }

    /**
     * Creates a cancel callback to the current progress indicator (if the current thread is a background cancellable thread)
     * @param onCancel a {@link Runnable} to be invoked when progress is cancelled
     */
    public static void cancelCallback(Runnable onCancel) {
        ProgressIndicator progressIndicator = ProgressMonitor.getProgressIndicator();
        if (progressIndicator == null) return;
        if (progressIndicator.isCanceled()) return;
        if (!progressIndicator.isRunning()) return;
        WeakRef<ProgressIndicator> indicator = WeakRef.of(progressIndicator);

        // self-closing timer, monitoring the thread-local progress indicator and closing when progress is no longer running
        Timer timer = new Timer("DBN - Progress Cancel Monitor", true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ProgressIndicator progressIndicator = indicator.get();
                if (progressIndicator == null || !progressIndicator.isRunning()) {
                    timer.cancel();
                    return;
                }
                if (progressIndicator.isCanceled()) onCancel.run();
            }
        }, 500, 500);
    }

}
