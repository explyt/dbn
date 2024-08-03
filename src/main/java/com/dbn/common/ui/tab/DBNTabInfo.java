package com.dbn.common.ui.tab;

import com.dbn.common.dispose.Disposer;
import com.intellij.openapi.Disposable;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;

@Getter
@Setter
public class DBNTabInfo<T> implements Disposable {
    private String title;
    private String tooltip;
    private Color color;
    private Icon icon;
    private T content;

    public DBNTabInfo(String title, String tooltip, Icon icon, T content) {
        this.title = title;
        this.tooltip = tooltip;
        this.icon = icon;
        this.content = content;
    }

    @Override
    public void dispose() {
        Disposer.dispose(content);
    }
}
