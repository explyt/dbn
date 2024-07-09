package com.dbn.editor.data.state.column.ui;

import com.dbn.common.icon.Icons;
import com.dbn.common.ui.list.Selectable;
import com.dbn.editor.data.state.column.DatasetColumnState;
import com.dbn.object.DBColumn;
import com.dbn.object.DBDataset;
import com.dbn.object.lookup.DBObjectRef;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Comparator;

public class ColumnStateSelectable implements Selectable {
    public static final Comparator<ColumnStateSelectable> NAME_COMPARATOR = Comparator.comparing(ColumnStateSelectable::getName);
    public static final Comparator<ColumnStateSelectable> POSITION_COMPARATOR = Comparator.comparingInt(ColumnStateSelectable::getOriginalPosition);

    private final DatasetColumnState state;
    private final DBObjectRef<DBDataset> dataset;

    public ColumnStateSelectable(DBDataset dataset, DatasetColumnState state) {
        this.dataset = DBObjectRef.of(dataset);
        this.state = state;
    }

    private DBDataset getDataset() {
        return DBObjectRef.get(dataset);
    }

    public DBColumn getColumn() {
        DBDataset dataset = getDataset();
        if (dataset != null) {
            return dataset.getColumn(state.getName());
        }
        return null;
    }

    @Override
    public Icon getIcon() {
        DBColumn column = getColumn();
        return column == null ? Icons.DBO_COLUMN : column.getIcon();
    }


    public int getOriginalPosition() {
        DBColumn column = getColumn();
        if (column != null) {
            return column.getPosition();
        }
        return 0;
    }

    @Override
    public @NotNull String getName() {
        return state.getName();
    }

    @Override
    public boolean isSecondary() {
        DBColumn column = getColumn();
        return column != null && column.isAudit();
    }

    @Override
    public String getError() {
        return null;
    }

    @Override
    public boolean isSelected() {
        return state.isVisible();
    }

    @Override
    public boolean isMasterSelected() {
        return true;
    }

    @Override
    public void setSelected(boolean selected) {
        state.setVisible(selected);
    }

    @Override
    public int compareTo(@NotNull Object o) {
        return 0;
    }

    public int getPosition() {
        return state.getPosition();
    }

    public void setPosition(short position) {
        state.setPosition(position);
    }
}
