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

package com.dbn.common.outcome;

import com.dbn.common.notification.NotificationGroup;
import com.dbn.diagnostics.Diagnostics;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Standard implementation of an {@link OutcomeHandlers} bundle
 * Holds a sorted container for each type of outcome, where the different {@link OutcomeHandler} entities can be registered
 * the {@link #handle(Outcome)} method is invoking all registered handlers matching the {@link OutcomeType} in their defined priority sequence
 *
 * @author Dan Cioca (Oracle)
 */
@Slf4j
public final class OutcomeHandlersImpl implements OutcomeHandlers {
    private final Map<OutcomeType, Set<OutcomeHandler>> handlers = new HashMap<>();

    @Override
    public void addHandler(OutcomeType type, OutcomeHandler handler) {
        handlers.computeIfAbsent(type, t -> new HashSet<>()).add(handler);
    }

    @Override
    public void addMessageHandler(OutcomeType type, Project project) {
        addHandler(type, MessageOutcomeHandler.get(project));
    }

    @Override
    public void addNotificationHandler(OutcomeType type, Project project, NotificationGroup group) {
        addHandler(type, NotificationOutcomeHandler.get(project, group));
    }

    @Override
    public void handle(Outcome outcome) {
        Set<OutcomeHandler> handlers = getHandlers(outcome);
        if (handlers == null) return;

        handlers.stream().sorted(Comparator.comparing(h -> h.getPriority())).forEach(handler -> handleSafe(outcome, handler));
    }

    private static void handleSafe(Outcome outcome, OutcomeHandler handler) {
        try {
            handler.handle(outcome);
        } catch (ProcessCanceledException e) {
            Diagnostics.conditionallyLog(e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private Set<OutcomeHandler> getHandlers(Outcome outcome) {
        return handlers.get(outcome.getType());
    }
}
