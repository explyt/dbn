package com.dbn.common.ui.tab;

import java.util.EventListener;

public interface TabsListener extends EventListener {
    void selectionChanged(int selectedIndex);
}
