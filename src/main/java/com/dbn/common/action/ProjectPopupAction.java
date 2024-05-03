package com.dbn.common.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.InputEvent;

import static java.util.Arrays.stream;

public abstract class ProjectPopupAction extends ProjectAction {
    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        DefaultActionGroup actionGroup = new DefaultActionGroup();

        AnAction[] children = getChildren(e);
        stream(children).forEach(a -> actionGroup.add(a));

        InputEvent inputEvent = e.getInputEvent();
        if (inputEvent != null) {
            Component component = (Component) inputEvent.getSource();
            if (component.isShowing()) {
                ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(
                        null,
                        actionGroup,
                        e.getDataContext(),
                        JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                        true, null, 10);

                //Project project = (Project) e.getDataContext().getData(DataConstants.PROJECT);
                DataProvider dataProvider = getDataProvider(e);
                if (dataProvider != null) {
                    DataProviders.register(popup.getContent(), dataProvider);
                }
                showBelowComponent(popup, component);
            }
        }
    }

    private static void showBelowComponent(ListPopup popup, Component component) {
        Point locationOnScreen = component.getLocationOnScreen();
        Point location = new Point(
                (int) (locationOnScreen.getX() + 10),
                (int) locationOnScreen.getY() + component.getHeight());
        popup.showInScreenCoordinates(component, location);
    }

    public DataProvider getDataProvider(AnActionEvent e) {
        return null;
    }

    public abstract AnAction[] getChildren(AnActionEvent e);
}
