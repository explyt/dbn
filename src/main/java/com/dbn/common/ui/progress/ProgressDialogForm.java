package com.dbn.common.ui.progress;

import com.dbn.common.ui.form.DBNFormBase;
import com.dbn.common.ui.listener.KeyAdapter;
import com.dbn.common.util.Conditional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class ProgressDialogForm extends DBNFormBase {
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JProgressBar progressBar;
    private JLabel progressTitleLabel;
    private JButton backgroundButton;
    private JButton cancelButton;

    public ProgressDialogForm(@NotNull ProgressDialogHandler handler) {
        super(null, handler.getProject());

        progressBar.setIndeterminate(true);
        progressBar.setVisible(true);
        progressTitleLabel.setText(handler.getText());

        KeyAdapter keyListener = createKeyListener(handler);
        cancelButton.addKeyListener(keyListener);
        backgroundButton.addKeyListener(keyListener);

        cancelButton.addActionListener(e -> handler.cancel());
        backgroundButton.addActionListener(e -> handler.release());
    }

    @NotNull
    private static KeyAdapter createKeyListener(@NotNull ProgressDialogHandler handler) {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                Conditional.when(e.getKeyCode() == KeyEvent.VK_ESCAPE, () -> handler.cancel());
            }
        };
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return backgroundButton;
    }

    @Override
    protected JComponent getMainComponent() {
        return mainPanel;
    }
}
