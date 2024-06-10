package com.dbn.oracleAI.config.ui.profiles;

import com.dbn.oracleAI.config.DBObjectItem;
import com.dbn.oracleAI.config.ProfileDBObjectItem;
import com.dbn.oracleAI.types.DatabaseObjectType;
import com.intellij.openapi.diagnostic.Logger;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Database object table model.
 * A model that manipulate list of <code>DBObjectItem</code>
 */
public class DatabaseObjectListTableModel extends AbstractTableModel {

    private static final Logger LOGGER = Logger.getInstance("com.dbn.oracleAI");
    static private final ResourceBundle messages = ResourceBundle.getBundle("Messages", Locale.getDefault());

    private static final int TABLES_COLUMN_HEADERS_NAME_IDX = 0;

    //items that do not match the object name pattern
    private List<DBObjectItem> allItems;

    //items that are already selected, moved to profile object list
    //These items will be parked out so to not be displayed
    List<DBObjectItem> parkedItems;

    private static final String[] columnNames = {
            messages.getString("profile.mgmt.obj_table.header.name")
    };

    /**
     * Creates a new model
     */
    public DatabaseObjectListTableModel() {
        this.allItems = new ArrayList<>();
        this.parkedItems = new ArrayList<>();
    }

    /**
     * Creates a new model with a given list of object.
     * @param objs list  of database objects  that will populate the model
     * @param hideViewsByDefault if true the model will hide views from the list of objects.
     */
    public DatabaseObjectListTableModel(List<DBObjectItem> objs, boolean hideViewsByDefault) {
        this.parkedItems = new ArrayList<>();
        if (hideViewsByDefault) {
            this.allItems = new ArrayList<>();
            for (DBObjectItem obj : objs) {
                if (obj.getType().equals(DatabaseObjectType.TABLE)) {
                    this.allItems.add(obj);
                } else {
                    // this is a view, we hide it by default
                    this.parkedItems.add(obj);
                }
            }

        } else {
            // include all
            this.allItems = objs;
        }
    }

    @Override
    public int getRowCount() {
        return this.allItems.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        DBObjectItem item = allItems.get(rowIndex);
        switch (columnIndex) {
            case TABLES_COLUMN_HEADERS_NAME_IDX:
                return item.getName();
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
        return String.class;
    }

    /**
     * Hides from the model all object of a given type
     * hidden ones will be kept as reference but won't be part of the actual model
     * @param databaseObjectType the type of object to be hidden
     */
    public void hideItemByType(DatabaseObjectType databaseObjectType) {
        LOGGER.debug("DatabaseObjectListTableModel.hideItemByType: " + databaseObjectType);
        List<DBObjectItem> matches = new ArrayList<>();
        allItems.stream().filter(item -> item.getType().equals(databaseObjectType)).allMatch(
                match -> matches.add(match)
        );
        if (matches.size() > 0) {
            parkedItems.addAll(matches);
            allItems.removeAll(matches);
            LOGGER.debug("DatabaseObjectListTableModel.hideItemByNames: triggering fireTableDataChanged");
            fireTableDataChanged();
        }
    }

    /**
     * unhides from the model all object of a given type
     * reveal ones will be put back as part of the actual model
     * @param databaseObjectType the type of object to be hidden
     */
    public void unhideItemByType(DatabaseObjectType databaseObjectType) {
        LOGGER.debug("DatabaseObjectListTableModel.unhideItemByType: " + databaseObjectType);
        List<DBObjectItem> matches = new ArrayList<>();
        parkedItems.stream().filter(item -> item.getType().equals(databaseObjectType)).allMatch(
                match -> matches.add(match)
        );
        if (matches.size() > 0) {
            parkedItems.removeAll(matches);
            allItems.addAll(matches);
            LOGGER.debug("DatabaseObjectListTableModel.unhideItemByType: triggering fireTableDataChanged");
            fireTableDataChanged();
        }
    }


    /**
     * Hides a item from the model
     * A model only contains item for a given schema, no name collision
     * can happen
     *
     * @param itemNames name of the object in the model to be hidden, can not be null
     */
    public void hideItemByNames(List<String> itemNames) {
        assert itemNames != null:"cannot be null";
        if (itemNames.size() == 0)
            return;
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("DatabaseObjectListTableModel.hideItemByNames: " + itemNames);
        List<DBObjectItem> matches = new ArrayList<>();
        allItems.stream().filter(item -> itemNames.contains(item.getName())).allMatch(
                match -> matches.add(match)
        );

        if (matches.size() > 0) {
            parkedItems.addAll(matches);
            allItems.removeAll(matches);
            LOGGER.debug("DatabaseObjectListTableModel.hideItemByNames: triggering fireTableDataChanged");
            fireTableDataChanged();
        }
    }

    /**
     * move away all items from the alreadySelectedItems list
     * @param item the item to be revealed
     */
    /**
     * Moves away all items from the alreadySelectedItems list that
     * match criterias
     *
     * @param itemOwner item owner
     * @param itemName  item name, can be null meaning "all"
     */
     void unhideItem(String itemOwner, String itemName) {
        // first deal with wild card
        // in that case, unhide all from the owner
        boolean removed = false;
        List<DBObjectItem> toBoMoved;
        if (itemName == null) {
            toBoMoved = parkedItems.stream().filter(
                    o -> o.getOwner().equalsIgnoreCase(itemOwner)).collect(Collectors.toList());
        } else {
            toBoMoved = parkedItems.stream().filter(
                    o -> o.getOwner().equalsIgnoreCase(itemOwner) &&
                            o.getName().equalsIgnoreCase(itemName)).collect(Collectors.toList());

        }
        if (toBoMoved.size() > 0) {
            parkedItems.removeAll(toBoMoved);
            allItems.addAll(toBoMoved);
            fireTableDataChanged();
        }
    }

    /**
     * Gets item from model at given indes
     * @param rowIndex the item indes
     * @return the item
     * @throws IndexOutOfBoundsException if index is < 0  or > model size
     */
    public DBObjectItem getItemAt(int rowIndex) throws IndexOutOfBoundsException {
        return allItems.get(rowIndex);
    }

    /**
     * find the first item in the model that is equivalent to givem profile item
     * see ProfileDBObjectItem.isEquivalentTo()
     * @param profileItem the profile DB item that must b e equivalent to intem in the model.
     * @return the matched item or Optional.empty();
     */
    public Optional<DBObjectItem> findFirst(ProfileDBObjectItem profileItem) {
        Optional<DBObjectItem> oitem = allItems.stream().filter(profileItem::isEquivalentTo).findFirst();
        return oitem;
    }
}
