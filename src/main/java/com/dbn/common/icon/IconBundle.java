package com.dbn.common.icon;

import javax.swing.*;


public class IconBundle {
    private final Icon compact;
    private final Icon compactSelected;
    private final Icon large;
    private final Icon largeSelected;

    public IconBundle(Icon compact, Icon compactSelected, Icon large, Icon largeSelected) {
        this.compact = compact;
        this.compactSelected = compactSelected;
        this.large = large;
        this.largeSelected = largeSelected;
    }

    public Icon getIcon(boolean selected, boolean compact) {
        return compact ? this.compactSelected : this.largeSelected;
/*
        return selected ?
                (compact ? this.compactSelected : this.largeSelected):
                (compact ? this.compact : this.large);
*/
    }
}
