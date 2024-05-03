package com.dbn.common.ui.util;

import com.intellij.openapi.ui.popup.IPopupChooserBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.popup.list.ListPopupImpl;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.List;

public class Popups {
    public static void showUnderneathOf(@NotNull JBPopup popup, @NotNull Component sourceComponent, int verticalShift, int maxHeight) {
        JComponent popupContent = popup.getContent();
        Dimension preferredSize = popupContent.getPreferredSize();
        int width = Math.max((int) preferredSize.getWidth(), sourceComponent.getWidth());
        int height = (int) Math.min(maxHeight, preferredSize.getHeight());

        if (popup instanceof ListPopupImpl) {
            ListPopupImpl listPopup = (ListPopupImpl) popup;
            JList list = listPopup.getList();
            int listHeight = (int) list.getPreferredSize().getHeight();
            if (listHeight > height) {
                height = Math.min(maxHeight, listHeight);
            }
        }

        popupContent.setPreferredSize(new Dimension(width, height));

        popup.show(new RelativePoint(sourceComponent, new Point(0, sourceComponent.getHeight() + verticalShift)));
    }

    public static <T> void showCompletionPopup(
            JComponent toolbarComponent,
            List<T> elements,
            String title,
            JTextComponent textField,
            String adText) {

        JBPopupFactory popupFactory = JBPopupFactory.getInstance();
        IPopupChooserBuilder<T> builder = popupFactory.createPopupChooserBuilder(elements);
        if (title != null) builder.setTitle(title);

        JBPopup popup = builder.
                setMovable(false).
                setResizable(false).
                setRequestFocus(true).
                setItemChosenCallback(e -> {
                    if (e == null) return;

                    textField.setText(e.toString());
                    IdeFocusManager.getGlobalInstance().requestFocus(textField, false);
                }).
                createPopup();

        if (adText != null) {
            popup.setAdText(adText, SwingConstants.LEFT);
        }

        if (toolbarComponent != null) {
            popup.showUnderneathOf(toolbarComponent);
        }
        else {
            popup.showUnderneathOf(textField);
        }
    }
}
