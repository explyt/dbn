package com.dbn.common.ui.shortcut;

import com.dbn.common.ui.util.Keyboard;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import static com.dbn.common.dispose.Checks.isValid;
import static com.intellij.openapi.actionSystem.AnAction.getEventProject;

@Getter
public abstract class ShortcutInterceptor implements AnActionListener {
    protected final String delegateActionId;
    protected final Class<? extends AnAction> delegateActionClass;

    public ShortcutInterceptor(String delegateActionId) {
        this.delegateActionId = delegateActionId;
        this.delegateActionClass = getDelegateAction().getClass();
    }

    protected AnAction getDelegateAction() {
        return ActionManager.getInstance().getAction(delegateActionId);
    }

    protected boolean matchesDelegateShortcuts(AnActionEvent event) {
        Shortcut[] shortcuts = Keyboard.getShortcuts(delegateActionId);
        return Keyboard.match(shortcuts, event);
    }

    protected boolean isValidContext(AnActionEvent event) {
        Project project = getEventProject(event);
        return isValid(project);
    }

    protected abstract boolean canDelegateExecute(AnActionEvent event);

    protected void invokeDelegateAction(@NotNull AnActionEvent event) {
        AnAction delegateAction = getDelegateAction();
        AnActionEvent delegateEvent = new AnActionEvent(
                event.getInputEvent(),
                event.getDataContext(),
                event.getPlace(),
                new Presentation(),
                ActionManager.getInstance(), 0);

        delegateAction.actionPerformed(delegateEvent);
    }
}
