package com.dbn.connection.config.ui;

import com.dbn.common.dispose.StatefulDisposable;
import com.dbn.connection.config.ConnectionBundleSettings;
import com.dbn.connection.config.ConnectionSettings;
import com.dbn.data.sorting.SortDirection;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class ConnectionListModel extends DefaultListModel<ConnectionSettings> implements StatefulDisposable {
    private static final Comparator<ConnectionSettings> ASC_COMPARATOR = Comparator.comparing(s -> s.getDatabaseSettings().getName());
    private static final Comparator<ConnectionSettings> DESC_COMPARATOR = (s1, s2) -> -s1.getDatabaseSettings().getName().compareTo(s2.getDatabaseSettings().getName());

    private boolean disposed;

    public ConnectionListModel(ConnectionBundleSettings connectionBundleSettings) {
        List<ConnectionSettings> connections = connectionBundleSettings.getConnections();
        for (ConnectionSettings connection : connections) {
            addElement(connection);
        }
    }

    public ConnectionSettings getConnectionConfig(@NonNls String name) {
        for (int i=0; i< getSize(); i++){
            ConnectionSettings connectionSettings = getElementAt(i);
            if (Objects.equals(connectionSettings.getDatabaseSettings().getName(), name)) {
                return connectionSettings;
            }
        }
        return null;
    }

    public void sort(SortDirection sortDirection) {
        List<ConnectionSettings> list = new ArrayList<>();
        for (int i=0; i< getSize(); i++){
            ConnectionSettings connectionSettings = getElementAt(i);
            list.add(connectionSettings);
        }

        switch (sortDirection) {
            case ASCENDING: list.sort(ASC_COMPARATOR); break;
            case DESCENDING: list.sort(DESC_COMPARATOR); break;
        }

        clear();
        for (ConnectionSettings connectionSettings : list) {
            addElement(connectionSettings);
        }
    }

    @Override
    public void disposeInner() {
        removeAllElements();
    }
}
