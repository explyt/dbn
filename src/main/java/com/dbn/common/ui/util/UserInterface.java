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

import com.dbn.common.compatibility.Compatibility;
import com.dbn.common.lookup.Visitor;
import com.dbn.common.thread.Dispatch;
import com.dbn.common.ui.ValueSelector;
import com.dbn.common.util.Environment;
import com.dbn.common.util.Strings;
import com.intellij.ide.ui.UISettings;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.ui.AncestorListenerAdapter;
import com.intellij.ui.border.IdeaTitledBorder;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.IllegalComponentStateException;
import java.awt.KeyboardFocusManager;
import java.awt.LayoutManager;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static com.dbn.common.Reflection.invokeMethod;
import static com.dbn.common.ui.util.Borderless.isBorderless;
import static com.dbn.common.util.Commons.nvl;
import static com.dbn.common.util.Unsafe.cast;
import static com.dbn.common.util.Unsafe.logged;
import static com.dbn.common.util.Unsafe.silent;
import static com.dbn.diagnostics.Diagnostics.conditionallyLog;

@UtilityClass
public class UserInterface {

    public static final String NEW_UI_REGISTRY_KEY = "ide.experimental.ui";
    public static final String NEW_UI_RELEASE_VERSION = "2022.3";

    @Getter
    @Compatibility
    private static final boolean newUI = silent(false, () -> evaluateNewUi());

    @Compatibility
    public static boolean isCompactMode() {
        //UISettings.getInstance().getCompactMode();
        return nvl(invokeMethod(UISettings.getInstance(), "getCompactMode"), true);
    }

    private static boolean evaluateNewUi() {
        return Environment.isIdeNewerThan(NEW_UI_RELEASE_VERSION) && Registry.is(NEW_UI_REGISTRY_KEY);
    }


    public static void stopTableCellEditing(JComponent root) {
        visitRecursively(root, component -> {
            if (component instanceof JTable) {
                JTable table = (JTable) component;
                TableCellEditor cellEditor = table.getCellEditor();
                if (cellEditor != null) {
                    cellEditor.stopCellEditing();
                }
            }
        });
    }

    /**
     * Invokes a runnable once when the given component is shown
     * @param component the @{@link Component} to consider visibility event for
     * @param runnable the @{@link Runnable} to be invoked when the component is shown
     */
    public static void whenFirstShown(JComponent component, Runnable runnable) {
        whenShown(component, runnable, true);
    }


    public static void whenShown(JComponent component, Runnable runnable, boolean first) {
        // one time invocation of the runnable when component is shown
        AtomicReference<AncestorListener> listenerRef = new AtomicReference<>();
        AncestorListener listener = new AncestorListenerAdapter() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                try {
                    runnable.run();
                } finally {
                    if (first) {
                        // remove the listener if only first shown time is to be considered
                        AncestorListener listener = listenerRef.get();
                        component.removeAncestorListener(listener);
                    }
                }

            }
        };
        listenerRef.set(listener);
        component.addAncestorListener(listener);
    }


    public static void removeBorders(JComponent root) {
        visitRecursively(root, component -> component.setBorder(null));
    }

    @Nullable
    public static Point getRelativeMouseLocation(Component component) {
        try {
            PointerInfo pointerInfo = MouseInfo.getPointerInfo();
            if (pointerInfo != null) {
                Point mouseLocation = pointerInfo.getLocation();
                return getRelativeLocation(mouseLocation, component);
            }
        } catch (IllegalComponentStateException e) {
            conditionallyLog(e);
        }
        return null;
    }
    
    public static Point getRelativeLocation(Point locationOnScreen, Component component) {
        Point componentLocation = component.getLocationOnScreen();
        Point relativeLocation = locationOnScreen.getLocation();
        relativeLocation.move(
                (int) (locationOnScreen.getX() - componentLocation.getX()), 
                (int) (locationOnScreen.getY() - componentLocation.getY()));
        return relativeLocation;
    }

    public static boolean isChildOf(Component component, Component child) {
        Component parent = child == null ? null : child.getParent();
        while (parent != null) {
            if (parent == component) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    public static boolean isFocused(Component component, boolean recursive) {
        if (component.isFocusOwner()) return true;
        if (recursive && component instanceof JComponent) {
            JComponent parentComponent = (JComponent) component;
            for (Component childComponent : parentComponent.getComponents()) {
                if (isFocused(childComponent, recursive)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isFocusableComponent(Component component) {
        if (!component.isFocusable()) return false;
        if (!component.isEnabled()) return false;

        return
            component instanceof JTextComponent ||
            component instanceof AbstractButton ||
            component instanceof ValueSelector ||
            component instanceof JComboBox ||
            component instanceof JList<?> ||
            component instanceof JTable;
    }

    public static void updateTitledBorder(JPanel panel) {
        Border border = panel.getBorder();
        if (border instanceof TitledBorder) {
            TitledBorder titledBorder = (TitledBorder) border;
            String title = titledBorder.getTitle();
            int indent = Strings.isEmpty(title) || ClientProperty.NO_INDENT.is(panel) ? 0 : 16;
            IdeaTitledBorder replacement = new IdeaTitledBorder(title, indent, Borders.EMPTY_INSETS);

            border = new CompoundBorder(Borders.topInsetBorder(8), replacement);
            panel.setBorder(border);
        }
    }

    public static void repaint(Component component) {
        Dispatch.run(true, () -> {
            component.revalidate();
            component.repaint();
        });
    }

    public static void repaintAndFocus(Component component) {
        Dispatch.run(true, () -> {
            component.revalidate();
            component.repaint();
            component.requestFocus();
        });
    }

    public static void requestFocus(@Nullable JComponent component) {
        if (component == null) return;
        Dispatch.run(component, () -> component.requestFocus());
    }

    @Compatibility
    public static void updateActionToolbars(JComponent component) {
        visitRecursively(component, c -> {
            ActionToolbar toolbar = ClientProperty.ACTION_TOOLBAR.get(c);
            if (toolbar != null) toolbar.updateActionsImmediately();
        });
    }

    public static void changePanelBackground(JPanel panel, Color background) {
        panel.setBackground(background);
        for (Component component : panel.getComponents()) {
            if (component instanceof JPanel) {
                JPanel childPanel = (JPanel) component;
                changePanelBackground(childPanel, background);
            }
        }
    }

    public static int ctrlDownMask() {
        return SystemInfo.isMac ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;
    }

    public static void visitRecursively(JComponent component, Visitor<JComponent> visitor) {
        logged(() -> visitor.visit(component));

        Component[] childComponents = component.getComponents();
        for (Component childComponent : childComponents) {
            if (childComponent instanceof JComponent) {
                visitRecursively((JComponent) childComponent, visitor);
            }

        }
    }

    public static <T extends JComponent> void visitRecursively(JComponent component, Class<T> type, Visitor<T> visitor) {
        if (type.isAssignableFrom(component.getClass())) {
            logged(() -> visitor.visit(cast(component)));
        }

        Component[] childComponents = component.getComponents();
        for (Component childComponent : childComponents) {
            if (childComponent instanceof JComponent) {
                visitRecursively((JComponent) childComponent, type, visitor);
            }

        }
    }

    public static void updateTitledBorders(JComponent component) {
        visitRecursively(component, JPanel.class, p -> updateTitledBorder(p));
    }

    public static void updateScrollPanes(JComponent component) {
        visitRecursively(component, JScrollPane.class, sp -> {
            sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            sp.setBorder(isBorderlessPane(sp) ? null : Borders.COMPONENT_OUTLINE_BORDER);

            sp.setRequestFocusEnabled(false);
            sp.setFocusable(false);

            JViewport viewport = sp.getViewport();
            Component view = viewport.getView();
            if (view instanceof JList || view instanceof JTree) {
                sp.setViewportBorder(Borders.insetBorder(4));
            }

            if (view instanceof JComponent) {
                JComponent viewComponent = (JComponent) view;
                viewComponent.setBorder(null);
            }
        });
    }

    private static boolean isBorderlessPane(JScrollPane scrollPane) {
        Component component = scrollPane.getViewport().getView();
        return isBorderless(component);
    }


    @Nullable
    public static <T extends JComponent> T getRootParentOfType(Component component, Class<T> type) {
        T root = null;
        Component parent = component.getParent();
        while (parent != null) {
            if (type.isAssignableFrom(parent.getClass())) {
                root = cast(parent);
            }
            parent = parent.getParent();
        }
        return root;
    }

    @Nullable
    public static <T extends JComponent> T getParentOfType(JComponent component, Class<T> type) {
        Component parent = component.getParent();
        while (parent != null) {
            if (type.isAssignableFrom(parent.getClass())) return cast(parent);
            parent = parent.getParent();
        }
        return null;
    }

    public static <T extends JComponent> T getParent(JComponent component, Predicate<Component> check) {
        Component parent = component.getParent();
        while (parent != null) {
            if (check.test(parent)) return cast(parent);
            parent = parent.getParent();
        }
        return null;
    }

    public static Dimension adjustDimension(Dimension dimension, int widthAdjustment, int heightAdjustment) {
        return new Dimension((int) dimension.getWidth() + widthAdjustment, (int) dimension.getHeight() + heightAdjustment);
    }


    public static void updateSplitPanes(JComponent component) {
        visitRecursively(component, JSplitPane.class, sp -> Splitters.replaceSplitPane(sp));
    }

    public static void setBackgroundRecursive(JComponent component, Color color) {
        component.setBackground(color);
        Component[] children = component.getComponents();
        Arrays
            .stream(children)
            .filter(child -> child instanceof JComponent)
            .map(child -> (JComponent) child)
            .forEach(child -> setBackgroundRecursive(child, color));

    }

    public static void replaceComponent(JComponent oldComponent, JComponent newComponent) {
        Container container = oldComponent.getParent();
        LayoutManager layout = container.getLayout();
        for (int i = 0; i < container.getComponentCount(); i++) {
            if (container.getComponent(i) != oldComponent) continue;

            if (layout instanceof GridLayoutManager) {
                GridLayoutManager gridLayout = (GridLayoutManager) layout;
                GridConstraints constraints = gridLayout.getConstraintsForComponent(oldComponent);
                container.remove(i);
                container.add(newComponent, constraints);
            } else {
                container.remove(i);
                container.add(newComponent, i);
            }

        }
    }

    public static int getComponentIndex(Container container, Component component) {
        for (int i = 0; i < container.getComponentCount(); i++) {
            if (container.getComponent(i) == component) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds a child component matching the criteria provided by the "check" {@link Predicate}
     * (the lookup is done recursively and returns the first component matching the criteria)
     *
     * @param <T> the type of the component expected in return
     * @param rootComponent the component where to start the lookup
     * @param componentType the type of component expected in result
     * @param check the {@link Predicate} defining the conditions the components must meet
     * @return the first component matching the given criteria
     */
    @Nullable
    public static <T extends JComponent> T findChildComponent(Component rootComponent, Class<T> componentType, Predicate<T> check) {
        if (!(rootComponent instanceof JComponent)) return null;
        JComponent component = cast(rootComponent);

        Component[] components = component.getComponents();
        for (Component child : components) {
            if (!(child instanceof JComponent)) continue;

            JComponent childComponent = (JComponent) child;
            if (componentType.isAssignableFrom(childComponent.getClass()) && check.test(cast(childComponent))) {
                return cast(child);
            }

            childComponent = findChildComponent(childComponent, componentType, check);
            if (childComponent != null) {
                return cast(childComponent);
            }
        }
        return null;
    }

    public static <T extends JComponent> T findChildComponent(Component rootComponent, Predicate<JComponent> check) {
        return cast(findChildComponent(rootComponent, JComponent.class, check));
    }

    public static <T extends JComponent> boolean hasChildComponent(Component rootComponent, Class<T> componentType, Predicate<T> check) {
        return findChildComponent(rootComponent, componentType, check) != null;
    }

    public static <T extends JComponent> boolean hasChildComponent(Component rootComponent, Predicate<JComponent> check) {
        return hasChildComponent(rootComponent, JComponent.class, check);
    }

    @Nullable
    public static JLabel getComponentLabel(@Nullable Component component) {
        if (component == null) return null;

        JPanel rootPanel = getRootParentOfType(component, JPanel.class);
        if (rootPanel == null) return null;

        return findChildComponent(rootPanel, JLabel.class, l -> l.getLabelFor() == component);
    }

    @Nullable
    public static String getComponentText(@Nullable Component component) {
        if (component == null) return null;

        if (component instanceof JLabel) {
            JLabel label = (JLabel) component;
            return label.getText();
        }

        if (component instanceof AbstractButton) {
            AbstractButton button = (AbstractButton) component;
            return button.getText();
        }

        if (component instanceof JTextComponent) {
            JTextComponent textComponent = (JTextComponent) component;
            return textComponent.getText();
        }

        if (component instanceof JComboBox) {
            JComboBox comboBox = (JComboBox) component;
            Object selectedItem = comboBox.getSelectedItem();
            return selectedItem == null ? null : selectedItem.toString();
        }

        return component.getName();
    }

    /**
     * Compares the text of a given JLabel with a specified string.
     *
     * @param label the JLabel whose text is to be compared; if the label's text is null, it is treated as an empty string
     * @param text the string to compare with the JLabel's text; if the string is null, it is treated as an empty string
     * @return true if the JLabel's text is equal to the specified string, false otherwise
     */
    public static boolean matchesText(JLabel label, String text) {
        String oldText = nvl(label.getText(), "");
        String newText = nvl(text, "");
        return oldText.equals(newText);
    }

    /**
     * Updates the background color of the parent container of the specified component
     * to match the background color of the component.
     *
     * @param component the JComponent whose background color will be propagated to its parent
     */
    public static void propagateBackgroundUp(JComponent component) {
        Container parent = component.getParent();
        parent.setBackground(component.getBackground());
    }

    /**
     * Transfers focus to the next focusable component, starting with the specified component.
     *
     * @param component the JComponent from which the focus traversal begins
     */
    public static void focusNextComponent(JComponent component) {
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.focusNextComponent(component);
    }

    /**
     * Transfers focus to the previous focusable component, starting with the specified component.
     *
     * @param component the JComponent from which the focus traversal begins
     */
    public static void focusPreviousComponent(JComponent component) {
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.focusPreviousComponent(component);
    }

    /**
     * Enables automatic selection of the first item in a {@link JList} when it gains focus,
     * if no item is already selected and the list is non-empty.
     *
     * @param list the {@link JList} for which focus-based selection should be enabled
     */
    public static void enableSelectOnFocus(JList list) {
        list.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (list.getSelectedIndex() != -1) return;
                if (list.getModel().getSize() <= 0) return;
                list.setSelectedIndex(0);
            }
        });
    }

    public static void enableSelectOnFocus(JTree tree) {
        tree.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (tree.getSelectionCount() > 0) return;
                if (tree.getRowCount() == 0) return;

                tree.setSelectionRow(0);
            }
        });
    }


}
