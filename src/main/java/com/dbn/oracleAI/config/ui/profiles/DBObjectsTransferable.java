package com.dbn.oracleAI.config.ui.profiles;

import com.dbn.object.DBDataset;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

/**
 * Transferable object for Database object and profile object
 * This class is used during drag and drop between tables.
 * This class maintain a list of <code>DBObjectItem</code> to be transferred
 */
public class DBObjectsTransferable implements Transferable {

    public static final DataFlavor ADD_FLAVOR = new DataFlavor(DBDataset.class, "Datasets");
    public static final DataFlavor REMOVE_FLAVOR = new DataFlavor(Void.class, "Remove datasets");

    private static final DataFlavor[] supportedFlavor = {ADD_FLAVOR};
    private final List<DBDataset> transferred;

    /**
     * Creates a new DatabaseObjectsTransferable with a given list of DB object.
     * @param transferred list of DB object that will be transferred
     */
    public DBObjectsTransferable(List<DBDataset> transferred) {
        this.transferred = transferred;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return supportedFlavor;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return supportedFlavor[0].equals(flavor);
    }

    @NotNull
    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        return this.transferred;
    }

    @Override
    public String toString() {
        return "DatabaseObjectsTransferable{" +
                "transfered=" + transferred +
                '}';
    }
}
