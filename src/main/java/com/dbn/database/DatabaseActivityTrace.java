package com.dbn.database;

import lombok.Getter;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import static com.dbn.common.util.TimeUtil.isOlderThan;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

public class DatabaseActivityTrace {
    private boolean supported = true;
    private int failedAttempts;
    private long lastAttempt;

    @Getter
    private SQLException exception;

    public void init() {
        lastAttempt = System.currentTimeMillis();
        if (retryIntervalLapsed()) reset();
    }

    public void fail(SQLException exception, boolean unsupported) {
        this.exception = exception;
        this.failedAttempts++;
        if (unsupported) this.supported = false;
    }

    public void release() {
        if (exception == null) reset();
    }

    public boolean canExecute() {
        // do not allow more than three attempts per retry-interval
        return failedAttempts < 3 || retryIntervalLapsed();
    }

    private void reset() {
        supported = true;
        exception = null;
        failedAttempts = 0;
    }

    private boolean retryIntervalLapsed() {
        // increase retry-interval for activities provisionally marked as unsupported
        // (e.g. missing grants to given system views translating as syntax error)
        TimeUnit delayUnit = supported ? SECONDS : MINUTES;
        return isOlderThan(lastAttempt, 5, delayUnit);
    }

}
