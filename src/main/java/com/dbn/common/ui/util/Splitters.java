/*
 * Copyright 2024 Oracle and/or its affiliates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dbn.common.ui.util;

import com.dbn.common.thread.Dispatch;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.util.ui.JBUI;

import javax.swing.JComponent;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import java.awt.Container;

import static com.dbn.common.ui.util.UserInterface.whenFirstShown;
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
        double dividerLocation = JBUI.scale(pane.getDividerLocation());
        boolean vertical = orientation == VERTICAL_SPLIT;
        Splitter splitter = ClientProperty.REGULAR_SPLITTER.is(pane) ? new JBSplitter(vertical) : new OnePixelSplitter(vertical);

        splitter.setFirstComponent(component1);
        splitter.setSecondComponent(component2);
        splitter.setShowDividerControls(pane.isOneTouchExpandable());
        splitter.setHonorComponentsMinimumSize(true);
        splitter.setDividerPositionStrategy(dividerLocation > 0 ?
                Splitter.DividerPositionStrategy.KEEP_FIRST_SIZE :
                Splitter.DividerPositionStrategy.KEEP_PROPORTION);

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


        if (dividerLocation > 0) {
            whenFirstShown(splitter, () -> {
                Dispatch.run(() -> {
                    double proportion;

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

            });
        }
    }
}
