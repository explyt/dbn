package com.dci.intellij.dbn.database.interfaces.queue;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.routine.Consumer;
import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import com.dci.intellij.dbn.common.thread.Threads;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionRef;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterfaceQueue;
import com.intellij.openapi.project.Project;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.LockSupport;

import static com.dci.intellij.dbn.database.interfaces.queue.InterfaceTask.COMPARATOR;
import static com.dci.intellij.dbn.database.interfaces.queue.InterfaceTaskStatus.*;

@Slf4j
public class InterfaceQueue extends StatefulDisposable.Base implements DatabaseInterfaceQueue {
    private static final ExecutorService MONITORS = Threads.newCachedThreadPool("DBN - Database Interface Monitor", true);

    private final BlockingQueue<InterfaceTask<?>> queue = new PriorityBlockingQueue<>(11, COMPARATOR);
    private final Consumer<InterfaceTask<?>> consumer;
    private final Counters counters = new Counters();
    private final ConnectionRef connection;
    private volatile boolean stopped;


    public InterfaceQueue(ConnectionHandler connection) {
        this(connection, null);
    }

    InterfaceQueue(@Nullable ConnectionHandler connection, Consumer<InterfaceTask<?>> consumer) {
        this.connection = ConnectionRef.of(connection);
        this.consumer = consumer == null ? new InterfaceQueueConsumer(this) : consumer;
        this.counters.running.addListener(value -> warnTaskLimits(value));

        MONITORS.submit(() -> monitorQueue());
    }

    private void warnTaskLimits(int value) {
        if (counters.running() > maxActiveTasks()) {
            log.warn("Active task limit exceeded: {} (expected max {})", counters.running(), maxActiveTasks());
        }
    }

    @Override
    @NotNull
    public ConnectionHandler getConnection(){
        return connection.ensure();
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public Counters counters() {
        return counters;
    }

    private boolean maxActiveTasksExceeded() {
        return counters.running() >= maxActiveTasks();
    }

    @Override
    public int maxActiveTasks() {
        if (connection == null) return 10;
        return getConnection().getSettings().getDetailSettings().getMaxConnectionPoolSize();
    }

    @Override
    public <R> R scheduleAndReturn(InterfaceTaskRequest request, ThrowableCallable<R, SQLException> callable) throws SQLException {
        return queue(request, true, callable).getResponse();
    }

    @Override
    public void scheduleAndWait(InterfaceTaskRequest request, ThrowableRunnable<SQLException> runnable) throws SQLException {
        queue(request, true, ThrowableCallable.from(runnable));
    }

    @Override
    public void scheduleAndForget(InterfaceTaskRequest request, ThrowableRunnable<SQLException> runnable) throws SQLException {
        queue(request, false, ThrowableCallable.from(runnable));
    }

    @NotNull
    private <T> InterfaceTask<T> queue(InterfaceTaskRequest request, boolean synchronous, ThrowableCallable<T, SQLException> callable) throws SQLException {
        InterfaceTask<T> task = new InterfaceTask<>(request, synchronous, callable);
        try {
            queue.add(task);
            counters.queued.increment();
            task.changeStatus(QUEUED);

            task.awaitCompletion();
            return task;
        } finally {
            task.changeStatus(RELEASED);
        }
    }

    /**
     * Start monitoring the queue
     */
    @SneakyThrows
    private void monitorQueue() {
        Thread monitor = Thread.currentThread();
        counters.running.addListener(value -> unparkMonitor(monitor));

        while (!stopped) {
            checkDisposed();
            boolean parked = parkMonitor();
            if (parked) continue;

            InterfaceTask<?> task = queue.take();

            counters.queued.decrement();
            task.changeStatus(DEQUEUED);

            consumer.accept(task);
            counters.running.increment();
            task.changeStatus(SCHEDULED);
        }
    }

    private boolean parkMonitor() {
        if (!maxActiveTasksExceeded()) return false;

        LockSupport.park(counters);
        return true;
    }

    private boolean unparkMonitor(Thread monitor) {
        if (maxActiveTasksExceeded()) return false;

        LockSupport.unpark(monitor);
        return true;
    }

    void executeTask(InterfaceTask<?> task) {
        try {
            task.execute();
        } finally {
            counters.running.decrement();
            counters.finished.increment();
            task.changeStatus(FINISHED);
        }
    }

    @Override
    protected void disposeInner() {
        stopped = true;
        queue.clear();
        counters.queued.reset();
    }

    public Project getProject() {
        return getConnection().getProject();
    }
}