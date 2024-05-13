package com.dbn.oracleAI.config.ui.profiles;

import com.dbn.oracleAI.config.DBObjectItem;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

public class DatabaseObjectsTransferable implements Transferable {

    public static final DataFlavor DatabaseObjectFlavor =
            new DataFlavor(DBObjectItem.class, "DB object");

    private static final DataFlavor[] supportedFlavor = {DatabaseObjectFlavor};
    private List<DBObjectItem> transfered;

    public DatabaseObjectsTransferable(List<DBObjectItem> transfered) {
        this.transfered = transfered;
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
        return this.transfered;
    }

    @Override
    public String toString() {
        return "DatabaseObjectsTransferable{" +
                "transfered=" + transfered +
                '}';
    }
}
