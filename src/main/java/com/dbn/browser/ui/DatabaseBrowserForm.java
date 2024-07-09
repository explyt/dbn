package com.dbn.browser.ui;

import com.dbn.browser.model.BrowserTreeNode;
import com.dbn.common.ui.form.DBNForm;
import com.dbn.common.ui.form.DBNFormBase;
import com.dbn.connection.ConnectionId;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public abstract class DatabaseBrowserForm extends DBNFormBase {

    public DatabaseBrowserForm(DBNForm parent) {
        super(parent);
    }

    @Nullable
    public abstract DatabaseBrowserTree getBrowserTree();

    public abstract void selectElement(BrowserTreeNode treeNode, boolean focus, boolean scroll);

    public abstract void selectConnection(ConnectionId connectionId);

    public abstract ConnectionId getSelectedConnection();

    public abstract void rebuildTree();
}
