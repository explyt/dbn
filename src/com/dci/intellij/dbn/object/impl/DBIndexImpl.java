package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.DBIndex;
import com.dci.intellij.dbn.object.common.DBObjectRelationType;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObjectImpl;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationListImpl;
import com.dci.intellij.dbn.object.common.list.loader.DBObjectListFromRelationListLoader;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.INVALIDABLE;
import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.SCHEMA_OBJECT;
import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.UNIQUE;

public class DBIndexImpl extends DBSchemaObjectImpl implements DBIndex {
    private DBObjectList<DBColumn> columns;

    DBIndexImpl(DBDataset dataset, ResultSet resultSet) throws SQLException {
        super(dataset, resultSet);
    }

    @Override
    protected void initObject(ResultSet resultSet) throws SQLException {
        name = resultSet.getString("INDEX_NAME");
        set(UNIQUE, resultSet.getString("IS_UNIQUE").equals("Y"));
    }

    public void initStatus(ResultSet resultSet) throws SQLException {
        boolean valid = resultSet.getString("IS_VALID").equals("Y");
        getStatus().set(DBObjectStatus.VALID, valid);
    }

    @Override
    public void initProperties() {
        properties.set(SCHEMA_OBJECT, true);
        properties.set(INVALIDABLE, true);
    }

    @Override
    protected void initLists() {
        super.initLists();
        DBDataset dataset = getDataset();
        if (dataset != null) {
            columns = initChildObjects().createSubcontentObjectList(DBObjectType.COLUMN, this, COLUMNS_LOADER, dataset, DBObjectRelationType.INDEX_COLUMN);
        }
    }

    public DBObjectType getObjectType() {
        return DBObjectType.INDEX;
    }

    public DBDataset getDataset() {
        return (DBDataset) getParentObject();
    }

    public List<DBColumn> getColumns() {
        return columns.getObjects();
    }

    public boolean isUnique() {
        return is(UNIQUE);
    }

    protected List<DBObjectNavigationList> createNavigationLists() {
        List<DBObjectNavigationList> objectNavigationLists = super.createNavigationLists();

        if (columns != null && columns.size() > 0) {
            objectNavigationLists.add(new DBObjectNavigationListImpl<DBColumn>("Columns", columns.getObjects()));
        }
        objectNavigationLists.add(new DBObjectNavigationListImpl<DBDataset>("Dataset", getDataset()));

        return objectNavigationLists;
    }

    public void buildToolTip(HtmlToolTipBuilder ttb) {
        ttb.append(true, getObjectType().getName(), true);
        ttb.createEmptyRow();
        super.buildToolTip(ttb);
    }

    /********************************************************
     *                   TreeeElement                       *
     * ******************************************************/

    public boolean isLeaf() {
        return true;
    }

    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return EMPTY_TREE_NODE_LIST;
    }

    /**
     * ******************************************************
     * Loaders                       *
     * *******************************************************
     */
    private static final DynamicContentLoader COLUMNS_LOADER = new DBObjectListFromRelationListLoader();
}
