package com.dbn.common.ui.util;

import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.OnePixelSplitter;

import javax.swing.*;
import java.awt.*;

import static javax.swing.JSplitPane.VERTICAL_SPLIT;

public class Splitters {
    private Splitters() {}

    public static void makeRegular(JSplitPane pane) {
        ClientProperty.REGULAR_SPLITTER.set(pane, true);
    }

    public static void replaceSplitPane(JSplitPane pane) {
        Container parent = pane.getParent();
        if (parent.getComponents().length != 1 && !(parent instanceof Splitter)) {
            return;
        }

        JComponent component1 = (JComponent) pane.getTopComponent();
        JComponent component2 = (JComponent) pane.getBottomComponent();
        int orientation = pane.getOrientation();

        boolean vertical = orientation == VERTICAL_SPLIT;
        Splitter splitter = ClientProperty.REGULAR_SPLITTER.is(pane) ? new JBSplitter(vertical) : new OnePixelSplitter(vertical);
        splitter.setFirstComponent(component1);
        splitter.setSecondComponent(component2);
        splitter.setShowDividerControls(pane.isOneTouchExpandable());
        splitter.setHonorComponentsMinimumSize(true);

        if (parent instanceof Splitter) {
            Splitter psplitter = (Splitter) parent;
            if (psplitter.getFirstComponent() == pane) {
                psplitter.setFirstComponent(splitter);
            } else {
                psplitter.setSecondComponent(splitter);
            }
        } else {
            parent.remove(0);
            parent.setLayout(new BorderLayout());
            parent.add(splitter, BorderLayout.CENTER);
        }

        if (pane.getDividerLocation() > 0) {
            UserInterface.whenShown(splitter, () -> {
                double proportion;
                double dividerLocation = pane.getDividerLocation();

                if (pane.getOrientation() == VERTICAL_SPLIT) {
                    double height = (parent.getHeight() - pane.getDividerSize());
                    proportion = height > 0 ? dividerLocation / height : 0;
                } else {
                    double width = (parent.getWidth() - pane.getDividerSize());
                    proportion = width > 0 ? dividerLocation / width : 0;
                }

                if (proportion > 0.0 && proportion < 1.0) {
                    splitter.setProportion((float) proportion);
                }
            });
        }
    }
}
