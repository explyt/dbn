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

package com.dbn.editor.session.ui;

import com.dbn.common.icon.Icons;
import com.dbn.common.ref.WeakRef;
import com.dbn.common.ui.component.DBNComponent;
import com.dbn.common.ui.form.DBNForm;
import com.dbn.common.ui.form.DBNFormBase;
import com.dbn.common.ui.tab.DBNTabbedPane;
import com.dbn.common.ui.tab.DBNTabs;
import com.dbn.connection.ConnectionHandler;
import com.dbn.database.DatabaseFeature;
import com.dbn.editor.session.SessionBrowser;
import com.dbn.editor.session.details.SessionDetailsTable;
import com.dbn.editor.session.details.SessionDetailsTableModel;
import com.dbn.editor.session.model.SessionBrowserModelRow;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class SessionBrowserDetailsForm extends DBNFormBase {
    private JPanel mainPanel;
    private JPanel sessionDetailsTabsPanel;
    private JBScrollPane sessionDetailsTablePane;
    private final SessionDetailsTable sessionDetailsTable;
    private final DBNTabbedPane<DBNForm> detailsTabbedPane;
    private JPanel explainPlanPanel;

    private final WeakRef<SessionBrowser> sessionBrowser;
    private final SessionBrowserCurrentSqlPanel currentSqlPanel;

    public SessionBrowserDetailsForm(@NotNull DBNComponent parent, SessionBrowser sessionBrowser) {
        super(parent);
        this.sessionBrowser = WeakRef.of(sessionBrowser);
        sessionDetailsTable = new SessionDetailsTable(this);
        sessionDetailsTablePane.setViewportView(sessionDetailsTable);

        detailsTabbedPane = new DBNTabbedPane<>(this);
        detailsTabbedPane.enableFocusInheritance();
        sessionDetailsTabsPanel.add(detailsTabbedPane, BorderLayout.CENTER);

        currentSqlPanel = new SessionBrowserCurrentSqlPanel(this, sessionBrowser);


        JComponent component = currentSqlPanel.getComponent();
        DBNTabs.initTabComponent(component, Icons.FILE_SQL_CONSOLE, null, currentSqlPanel);

        detailsTabbedPane.addTab("Current Statement", component);

        ConnectionHandler connection = getConnection();
        if (DatabaseFeature.EXPLAIN_PLAN.isSupported(connection)) {
            explainPlanPanel = new JPanel(new BorderLayout());
            //explainPlanTabInfo.setObject(currentSqlPanel);
            detailsTabbedPane.addTab("Explain Plan", Icons.EXPLAIN_PLAN_RESULT, new JPanel());
        }

        detailsTabbedPane.addTabSelectionListener(i -> {
            String title = detailsTabbedPane.getTitleAt(i);
            if (title.equals("Explain Plan")) {
                // TODO
            }
        });
    }

    @NotNull
    private ConnectionHandler getConnection() {
        return getSessionBrowser().getConnection();
    }

    @NotNull
    public SessionBrowser getSessionBrowser() {
        return sessionBrowser.ensure();
    }

    public void update(@Nullable final SessionBrowserModelRow selectedRow) {
        SessionDetailsTableModel model = new SessionDetailsTableModel(selectedRow);
        sessionDetailsTable.setModel(model);
        sessionDetailsTable.adjustColumnWidths();
        currentSqlPanel.loadCurrentStatement();
    }

    public SessionBrowserCurrentSqlPanel getCurrentSqlPanel() {
        return currentSqlPanel;
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }
}
