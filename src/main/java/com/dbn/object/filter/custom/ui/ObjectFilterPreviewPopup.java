package com.dbn.object.filter.custom.ui;

import com.dbn.common.ui.StatementViewerPopup;
import com.dbn.object.filter.custom.ObjectFilter;

public class ObjectFilterPreviewPopup extends StatementViewerPopup {
    public ObjectFilterPreviewPopup(ObjectFilter<?> filter) {
        super(null, filter.createPreviewFile(), filter.getConnection());
    }
}
