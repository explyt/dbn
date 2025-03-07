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

import lombok.SneakyThrows;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class ReadWriteMonitor {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @SneakyThrows
    public <T> T read(Callable<T> callable) {
        Lock readLock = this.lock.readLock();
        try {
            readLock.lock();
            return callable.call();
        } finally {
            readLock.unlock();
        }
    }

    public void write(Runnable runnable) {
        Lock writeLock = this.lock.writeLock();
        try {
            writeLock.lock();
            runnable.run();
        } finally {
            writeLock.unlock();
        }
    }
}
