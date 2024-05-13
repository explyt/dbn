package com.dbn.oracleAI.config.ui.profiles;

import com.dbn.oracleAI.config.ProfileDBObjectItem;
import com.intellij.openapi.diagnostic.Logger;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * profile object table model.
 * A model that manipulate list of <code>ProfileDBObjectItem</code>
 */
public class ProfileObjectListTableModel extends AbstractTableModel {

    private static final Logger LOGGER = Logger.getInstance("com.dbn.oracleAI");
    static private final ResourceBundle messages = ResourceBundle.getBundle("Messages", Locale.getDefault());

    private List<ProfileDBObjectItem> data;

    private static final int NAME_COLUMN_IDX = 0;
    private static final int OWNER_COLUMN_IDX = 1;

    private static final String[] columnNames = {
            messages.getString("profile.mgmt.obj_table.header.name"),
            messages.getString("profile.mgmt.obj_table.header.owner")
    };

    /**
     * Creates a new (empty) model
     */
    public ProfileObjectListTableModel() {
        this.data = new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ProfileDBObjectItem item = data.get(rowIndex);
        switch (columnIndex) {
            case NAME_COLUMN_IDX:
                return item.getName();
            case OWNER_COLUMN_IDX:
                return item.getOwner();
            default:
                return null;
        }
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 1) {
            return String.class;
        }
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }


    /**
     * Adds a list of profile db object to the model
     * @param items the list of objects to be added
     */
    public void addItems(List<ProfileDBObjectItem> items) {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("ProfileObjectListTableModel.addItems: " + items);
        int curRow = data.size();
        data.addAll(items);
        LOGGER.debug(
                "ProfileObjectListTableModel.addItems triggered  fireTableRowsInserted on (" +
                        curRow + "/" + curRow + items.size() + ")");
        fireTableRowsInserted(curRow, curRow + items.size());
    }

    /**
     * Removed a  profile db object from the model
     * @param item the  object to be removed
     */
    public void removeItem(ProfileDBObjectItem item) {
        LOGGER.debug("ProfileObjectListTableModel.removeItem: " + item);
        int index = data.indexOf(item);
        if (index >= 0) {
            data.remove(index);
            LOGGER.debug(
                    "ProfileObjectListTableModel.removeItem triggered  fireTableRowsDeleted on (" +
                            index + "/" + index + ")");
            fireTableRowsDeleted(index, index);
        }
    }

    /**
     * Replaces list of object in the model
     * @param items the new item list to be added to the model.
     */
    public void updateItems(List<ProfileDBObjectItem> items) {
        data.clear();
        data.addAll(items);
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("ProfileObjectListTableModel.updateItems: " + items);
        fireTableDataChanged();
    }

    // Method to get data

    /**
     * Gets the list of items of that model
     * @return the list of profile DB object
     */
    public List<ProfileDBObjectItem> getData() {
        return data;
    }


    /**
     * Gets an item from the model at a given index
     * @param rowIndex the index within the model
     * @return the item at given index
     * @throws IndexOutOfBoundsException if index is not valid
     */
    public ProfileDBObjectItem getItemAt(int rowIndex) throws IndexOutOfBoundsException{
        return data.get(rowIndex);
    }
}
