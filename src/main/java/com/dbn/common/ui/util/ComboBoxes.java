package com.dbn.common.ui.util;

import com.dbn.common.ui.Presentable;
import com.dbn.common.ui.misc.DBNComboBoxModel;
import com.intellij.ui.ColoredListCellRenderer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ComboBoxes {
    public static void addItems(JComboBox comboBox, Iterable items) {
        for (Object item : items) {
            comboBox.addItem(item);
        }
    }

    public static void addItems(DefaultComboBoxModel comboBox, Iterable items) {
        for (Object item : items) {
            comboBox.addElement(item);
        }
    }

    public static <T extends Presentable> void initComboBox(JComboBox<T> comboBox, T... options) {
        initComboBox(comboBox, Arrays.asList(options));
    }

    public static <T extends Presentable> void initComboBox(JComboBox<T> comboBox, Collection<T> options) {
        DBNComboBoxModel<T> model = new DBNComboBoxModel<>();
        model.getItems().addAll(options);
        comboBox.setModel(model);
        comboBox.setRenderer(new ColoredListCellRenderer<>() {
            @Override
            protected void customizeCellRenderer(@NotNull JList<? extends T> list, T value, int index, boolean selected, boolean hasFocus) {
                if (value != null) {
                    append(value.getName());
                    setIcon(value.getIcon());
                }
            }
        });
    }

    public static <T> T getSelection(JComboBox<T> comboBox) {
        return (T) comboBox.getSelectedItem();
    }

    public static <T> void setSelection(JComboBox<T> comboBox, T value) {
        comboBox.setSelectedItem(value);
    }

    public static <T> List<T> getElements(JComboBox<T> comboBox) {
        List<T> list = new ArrayList<>();
        ComboBoxModel<T> model = comboBox.getModel();
        for (int i = 0; i< model.getSize(); i++) {
            T element = model.getElementAt(i);
            if (element != null) {
                list.add(element);
            }
        }

        return list;
    }

}
