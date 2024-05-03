package com.dbn.data.find.action;


import com.dbn.common.action.BasicAction;
import com.dbn.data.find.DataSearchComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.Shortcut;
import lombok.Getter;

import javax.swing.*;
import java.util.Set;

@Getter
public abstract class DataSearchHeaderAction extends BasicAction {
    private final DataSearchComponent searchComponent;

    protected DataSearchHeaderAction(DataSearchComponent searchComponent) {
        this.searchComponent = searchComponent;
    }

    protected static void registerShortcutsToComponent(Set<Shortcut> shortcuts, AnAction action, JComponent component) {
        CustomShortcutSet shortcutSet = new CustomShortcutSet(shortcuts.toArray(new Shortcut[0]));
        action.registerCustomShortcutSet(shortcutSet, component);
    }
}

