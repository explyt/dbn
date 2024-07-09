package com.dbn.debugger;

import com.dbn.common.thread.Threads;
import com.dbn.database.interfaces.DatabaseInterface.Runnable;
import com.dbn.nls.NlsResources;
import com.intellij.openapi.project.Project;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;

import static com.dbn.common.notification.NotificationGroup.DEBUGGER;
import static com.dbn.common.notification.NotificationSupport.sendErrorNotification;
import static com.dbn.diagnostics.Diagnostics.conditionallyLog;
import static com.dbn.nls.NlsResources.txt;

@UtilityClass
public class DBDebugOperation {

    public static void run(@NotNull Project project, String title, Runnable runnable) {
        ExecutorService executorService = Threads.debugExecutor();
        executorService.submit( () -> {
            Thread currentThread = Thread.currentThread();
            int initialPriority = currentThread.getPriority();
            currentThread.setPriority(Thread.MIN_PRIORITY);
            try {
                runnable.run();
            } catch (Exception e) {
                conditionallyLog(e);
                sendErrorNotification(project, DEBUGGER, txt("ntf.debugger.error.ErrorPerformingOperation", title, e));
            } finally {
                currentThread.setPriority(initialPriority);
            }
        });
    }


}
