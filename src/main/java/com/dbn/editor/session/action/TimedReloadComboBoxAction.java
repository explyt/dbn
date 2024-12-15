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

package com.dbn.editor.session.action;

import com.dbn.common.action.BasicAction;
import com.dbn.common.action.ComboBoxAction;
import com.dbn.common.action.DataKeys;
import com.dbn.common.action.Lookups;
import com.dbn.common.icon.Icons;
import com.dbn.common.util.Context;
import com.dbn.editor.session.SessionBrowser;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import javax.swing.JComponent;

import static com.dbn.nls.NlsResources.txt;

public class TimedReloadComboBoxAction extends ComboBoxAction implements DumbAware {

    public TimedReloadComboBoxAction() {
    }

    @Override
    @NotNull
    protected DefaultActionGroup createPopupActionGroup(JComponent component, DataContext dataContext) {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new SelectRefreshTimeAction(0));
        actionGroup.addSeparator();
        actionGroup.add(new SelectRefreshTimeAction(5));
        actionGroup.add(new SelectRefreshTimeAction(10));
        actionGroup.add(new SelectRefreshTimeAction(20));
        actionGroup.add(new SelectRefreshTimeAction(30));
        actionGroup.add(new SelectRefreshTimeAction(60));
        return actionGroup;
    }

    @Override
    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        Icon icon = Icons.ACTION_TIMED_REFRESH_OFF;
        String text = "No Refresh";


        SessionBrowser sessionBrowser = getSessionBrowser(e);
        if (sessionBrowser != null) {
            int refreshInterval = sessionBrowser.getRefreshInterval();
            if (refreshInterval > 0) {
                text = refreshInterval + " seconds";
                if (sessionBrowser.isPreventLoading(false)) {
                    icon = Icons.ACTION_TIMED_REFRESH_INTERRUPTED;
                } else {
                    icon = Icons.ACTION_TIMED_REFRESH;
                }

            }
        }

        presentation.setText(text);
        presentation.setIcon(icon);
    }

    @Nullable
    public static SessionBrowser getSessionBrowser(JComponent component) {
        DataContext dataContext = Context.getDataContext(component);
        SessionBrowser sessionBrowser = DataKeys.SESSION_BROWSER.getData(dataContext);
        if (sessionBrowser == null) {
            FileEditor fileEditor = Lookups.getFileEditor(dataContext);
            if (fileEditor instanceof SessionBrowser) {
                sessionBrowser = (SessionBrowser) fileEditor;
            }
        }
        return sessionBrowser;
    }

    @Nullable
    public static SessionBrowser getSessionBrowser(AnActionEvent e) {
        SessionBrowser sessionBrowser = e.getData((DataKeys.SESSION_BROWSER));
        if (sessionBrowser == null) {
            FileEditor fileEditor = Lookups.getFileEditor(e);
            if (fileEditor instanceof SessionBrowser) {
                sessionBrowser = (SessionBrowser) fileEditor;
            }
        }
        return sessionBrowser;
    }

    private static class SelectRefreshTimeAction extends BasicAction {
        private final int seconds;

        SelectRefreshTimeAction(int seconds) {
            super(seconds == 0 ?
                    txt("app.dataEditor.action.NoRefresh"):
                    txt("app.dataEditor.action.RefreshSeconds", seconds), null, seconds == 0 ? null : Icons.COMMON_TIMER);
            this.seconds = seconds;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            SessionBrowser sessionBrowser = getSessionBrowser(e);
            if (sessionBrowser != null) {
                sessionBrowser.setRefreshInterval(seconds);
            }
        }
    }
 }