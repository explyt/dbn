package com.dbn.object.properties.ui;

import com.dbn.browser.DatabaseBrowserManager;
import com.dbn.browser.model.BrowserTreeEventListener;
import com.dbn.browser.model.BrowserTreeNode;
import com.dbn.browser.ui.DatabaseBrowserTree;
import com.dbn.common.dispose.Disposer;
import com.dbn.common.event.ProjectEvents;
import com.dbn.common.thread.Background;
import com.dbn.common.thread.Dispatch;
import com.dbn.common.thread.PooledThread;
import com.dbn.common.ui.form.DBNForm;
import com.dbn.common.ui.form.DBNFormBase;
import com.dbn.common.ui.util.UserInterface;
import com.dbn.common.util.Naming;
import com.dbn.object.common.DBObject;
import com.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class ObjectPropertiesForm extends DBNFormBase {
    private JPanel mainPanel;
    private JLabel objectLabel;
    private JLabel objectTypeLabel;
    private JScrollPane objectPropertiesScrollPane;
    private JPanel closeActionPanel;
    private DBObjectRef<?> object;

    private final AtomicReference<PooledThread> refreshHandle = new AtomicReference<>();
    private final ObjectPropertiesTable objectPropertiesTable;

    public ObjectPropertiesForm(DBNForm parent) {
        super(parent);
        //ActionToolbar objectPropertiesActionToolbar = ActionUtil.createActionToolbar("", true, "DBNavigator.ActionGroup.Browser.ObjectProperties");
        //closeActionPanel.add(objectPropertiesActionToolbar.getComponent(), BorderLayout.CENTER);
        objectPropertiesTable = new ObjectPropertiesTable(this, new ObjectPropertiesTableModel());
        objectPropertiesScrollPane.setViewportView(objectPropertiesTable);
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
                if (!browserManager.getShowObjectProperties().value()) return;

                DatabaseBrowserTree activeBrowserTree = browserManager.getActiveBrowserTree();
                if (activeBrowserTree == null) return;

                BrowserTreeNode treeNode = activeBrowserTree.getSelectedNode();
                if (treeNode instanceof DBObject) {
                    DBObject object = (DBObject) treeNode;
                    setObject(object);
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
        if (Objects.equals(object, localObject)) return;

        this.object = DBObjectRef.of(object);
        Background.run(getProject(), refreshHandle, () -> {
            ObjectPropertiesTableModel tableModel = new ObjectPropertiesTableModel(object.getPresentableProperties());
            Disposer.register(ObjectPropertiesForm.this, tableModel);

            Dispatch.run(() -> {
                objectLabel.setText(object.getName());
                objectLabel.setIcon(object.getIcon());
                objectTypeLabel.setText(Naming.capitalize(object.getTypeName()) + ":");

                ObjectPropertiesTableModel oldTableModel = (ObjectPropertiesTableModel) objectPropertiesTable.getModel();
                objectPropertiesTable.setModel(tableModel);
                objectPropertiesTable.accommodateColumnsSize();

                UserInterface.repaint(mainPanel);
                Disposer.dispose(oldTableModel);
            });
        });
    }
}
