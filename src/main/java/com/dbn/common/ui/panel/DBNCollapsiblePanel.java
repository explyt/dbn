package com.dbn.common.ui.panel;

import com.dbn.common.event.ToggleListener;
import com.dbn.common.ui.component.DBNComponent;
import com.dbn.common.ui.form.DBNCollapsibleForm;
import com.dbn.common.ui.form.DBNFormBase;
import com.dbn.common.ui.util.Listeners;
import com.dbn.common.ui.util.Mouse;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

import static com.dbn.common.util.Conditional.when;

public class DBNCollapsiblePanel extends DBNFormBase {
    private JLabel toggleLabel;
    private JPanel contentPanel;
    private JPanel mainPanel;
    private JLabel toggleDetailLabel;
    private boolean expanded;
    private final DBNCollapsibleForm contentForm;

    private final Listeners<ToggleListener> listeners = Listeners.create(this);

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    public DBNCollapsiblePanel(@NotNull DBNComponent parent, DBNCollapsibleForm contentForm, boolean expanded) {
        super(parent);
        this.contentForm = contentForm;
        this.expanded = expanded;
        this.contentPanel.add(contentForm.getComponent(), BorderLayout.CENTER);

        Cursor handCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        this.toggleLabel.setCursor(handCursor);
        this.toggleDetailLabel.setCursor(handCursor);
        updateVisibility();

        Mouse.Listener mouseListener = Mouse.listener().onClick(e -> when(
                e.getClickCount() == 1 &&
                        e.getButton() == MouseEvent.BUTTON1, () -> toggleVisibility()));
        this.toggleLabel.addMouseListener(mouseListener);
        this.toggleDetailLabel.addMouseListener(mouseListener);
    }

    private void toggleVisibility() {
        setExpanded(!expanded);
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        updateVisibility();
        listeners.notify(l -> l.toggled(expanded));
    }

    private void updateVisibility() {
        contentPanel.setVisible(expanded);
        toggleDetailLabel.setVisible(!expanded);
        toggleDetailLabel.setText(contentForm.getCollapsedTitleDetail());
        toggleLabel.setIcon(expanded ? UIUtil.getTreeExpandedIcon() : UIUtil.getTreeCollapsedIcon());
        toggleLabel.setText(expanded ? contentForm.getExpandedTitle() : contentForm.getCollapsedTitle());
    }

    public void addToggleListener(ToggleListener listener) {
        listeners.add(listener);
    }
}
