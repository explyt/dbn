package com.dci.intellij.dbn.object.properties.ui;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.browser.model.BrowserTreeEventListener;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.ui.DatabaseBrowserTree;
import com.dci.intellij.dbn.common.color.Colors;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.ui.form.DBNForm;
import com.dci.intellij.dbn.common.ui.form.DBNFormBase;
import com.dci.intellij.dbn.common.ui.util.Borders;
import com.dci.intellij.dbn.common.ui.util.UserInterface;
import com.dci.intellij.dbn.common.util.Naming;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class ObjectPropertiesForm extends DBNFormBase {
    private JPanel mainPanel;
    private JLabel objectLabel;
    private JLabel objectTypeLabel;
    private JTable objectPropertiesTable;
    private JScrollPane objectPropertiesScrollPane;
    private JPanel closeActionPanel;
    private DBObjectRef<?> object;

    private final AtomicReference<Thread> refreshHandle = new AtomicReference<>();

    public ObjectPropertiesForm(DBNForm parent) {
        super(parent);
        //ActionToolbar objectPropertiesActionToolbar = ActionUtil.createActionToolbar("", true, "DBNavigator.ActionGroup.Browser.ObjectProperties");
        //closeActionPanel.add(objectPropertiesActionToolbar.getComponent(), BorderLayout.CENTER);
        objectPropertiesTable.setRowSelectionAllowed(false);
        objectPropertiesTable.setCellSelectionEnabled(true);
        objectPropertiesScrollPane.getViewport().setBackground(Colors.getTableBackground());
        objectPropertiesScrollPane.setBorder(Borders.EMPTY_BORDER);
        objectTypeLabel.setText("Object properties:");
        objectLabel.setText("(no object selected)");

        Project project = ensureProject();
        ProjectEvents.subscribe(project, this, BrowserTreeEventListener.TOPIC, browserTreeEventListener());
    }

    @NotNull
    private BrowserTreeEventListener browserTreeEventListener() {
        return new BrowserTreeEventListener() {
            @Override
            public void selectionChanged() {
                Project project = ensureProject();
                DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
                if (browserManager.getShowObjectProperties().value()) {
                    DatabaseBrowserTree activeBrowserTree = browserManager.getActiveBrowserTree();
                    if (activeBrowserTree != null) {
                        BrowserTreeNode treeNode = activeBrowserTree.getSelectedNode();
                        if (treeNode instanceof DBObject) {
                            DBObject object = (DBObject) treeNode;
                            setObject(object);
                        }
                    }
                }
            }
        };
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    public DBObject getObject() {
        return DBObjectRef.get(object);
    }

    public void setObject(@NotNull DBObject object) {
        DBObject localObject = getObject();
        if (!Objects.equals(object, localObject)) {
            this.object = DBObjectRef.of(object);
            Project project = object.getProject();
            Progress.background(project, "Rendering object properties", refreshHandle, progress -> {
                ObjectPropertiesTableModel tableModel = new ObjectPropertiesTableModel(object.getPresentableProperties());
                Disposer.register(ObjectPropertiesForm.this, tableModel);

                Dispatch.run(() -> {
                    objectLabel.setText(object.getName());
                    objectLabel.setIcon(object.getIcon());
                    objectTypeLabel.setText(Naming.capitalize(object.getTypeName()) + ":");

                    ObjectPropertiesTable objectPropertiesTable = getObjectPropertiesTable();
                    ObjectPropertiesTableModel oldTableModel = (ObjectPropertiesTableModel) objectPropertiesTable.getModel();
                    objectPropertiesTable.setModel(tableModel);
                    objectPropertiesTable.accommodateColumnsSize();

                    UserInterface.repaint(mainPanel);
                    SafeDisposer.dispose(oldTableModel, false);
                });

            });
        }
    }

    public ObjectPropertiesTable getObjectPropertiesTable() {
        return (ObjectPropertiesTable) Failsafe.nn(objectPropertiesTable);
    }

    private void createUIComponents() {
        objectPropertiesTable = new ObjectPropertiesTable(this, new ObjectPropertiesTableModel());
        objectPropertiesTable.getTableHeader().setReorderingAllowed(false);
        Disposer.register(this, (Disposable) objectPropertiesTable);
    }
}