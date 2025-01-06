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

import com.intellij.openapi.actionSystem.ActionToolbar;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nls;

import javax.accessibility.AccessibleContext;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import static com.dbn.common.ui.util.UserInterface.getComponentLabel;

/**
 * Component accessibility utilities
 * Wrapper for accessible context. Acts more like a marker for identifying modules supporting accessibility.
 *
 * @author Dan Cioca (Oracle)
 */
@UtilityClass
public class Accessibility {

    public static void setAccessibleName(ActionToolbar toolbar, String name) {
        setAccessibleName(toolbar.getComponent(), name);
    }

    public static void setAccessibleDescription(ActionToolbar toolbar, String description) {
        setAccessibleDescription(toolbar.getComponent(), description);
    }

    public static void setAccessibleName(JComponent component, String name) {
        String friendlyName = name.replace("_", " ");
        AccessibleContext accessibleContext = component.getAccessibleContext();
        accessibleContext.setAccessibleName(friendlyName);
    }

    public static void setAccessibleDescription(JComponent component, String description) {
        String friendlyDescription = description.replace("_", " ");
        AccessibleContext accessibleContext = component.getAccessibleContext();
        accessibleContext.setAccessibleDescription(friendlyDescription);
    }

    public static void setAccessibleUnit(JTextField textField, @Nls String unit, @Nls String ... qualifiers) {
        JLabel label = getComponentLabel(textField);
        if (label != null) setAccessibleUnit(label, unit, qualifiers);
    }

    private static void setAccessibleUnit(JLabel label, @Nls String unit, @Nls String ... qualifiers) {
        AccessibleContext accessibleContext = label.getAccessibleContext();

        StringBuilder accessibleName = new StringBuilder(accessibleContext.getAccessibleName());
        accessibleName.append(" (");
        accessibleName.append(unit);
        if (qualifiers.length > 0) {
            accessibleName.append(" - ");
            accessibleName.append(String.join(", ", qualifiers));
        }

        accessibleName.append("(");
        accessibleContext.setAccessibleName(accessibleName.toString());
    }
}
