package com.dbn.common.action;

import com.dbn.common.Reflection;
import com.dbn.common.compatibility.Compatibility;
import com.dbn.common.compatibility.Workaround;
import com.dbn.common.util.Unsafe;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

/**
 * workaround for action groups which can themselves perform if invoked
 * "canBePerformed" was decommissioned and replaced with "setPerformGroup"
 */
@Workaround
public abstract class PerformableActionGroup extends ActionGroup implements BackgroundUpdatedAction, DumbAware {

    private static final Method SET_PERFORM_GROUP_METHOD = Reflection.findMethod(Presentation.class, "setPerformGroup", new Class[]{boolean.class});

    //@Override
    @Compatibility
    public boolean canBePerformed(@NotNull DataContext context) {
        return true;
    }

    @Override
    @Compatibility
    public void update(@NotNull AnActionEvent e) {
        Method method = SET_PERFORM_GROUP_METHOD;
        if (method == null) return;

        //e.getPresentation().setPerformGroup(true);
        Unsafe.silent(() -> Reflection.invokeMethod(e.getPresentation(), method, true));

    }
}
