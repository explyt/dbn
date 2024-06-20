package com.dbn.oracleAI.utils;

import javax.swing.JPanel;
import java.awt.Component;

public class RollingJPanelContainer<E> extends JPanel {
    private FixedSizeList<E> items;
    private int maxCapacity = -1;

    public void setMaxCapacity(int capacity) {
        assert maxCapacity == -1:"setMaxCapacity already called";
        maxCapacity = capacity;
        items = new FixedSizeList<E>(capacity);
    }

    private void ensureFreeSlot(int howMany) {
        int currentSize = items.size();
        int s = maxCapacity - currentSize - howMany;
        while (s++ < 0) {
            this.remove(0);
        }
    }

    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        assert maxCapacity != -1:"setMaxCapacity not called";

        ensureFreeSlot(1);
        super.addImpl(comp, constraints, index);
    }

}
