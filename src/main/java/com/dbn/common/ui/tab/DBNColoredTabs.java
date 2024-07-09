package com.dbn.common.ui.tab;

import com.dbn.common.compatibility.Workaround;
import com.dbn.common.dispose.Disposer;
import com.dbn.common.dispose.StatefulDisposable;
import com.dbn.common.thread.Dispatch;
import com.dbn.common.ui.form.DBNForm;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBEditorTabs;
import com.intellij.util.containers.ContainerUtil;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
public class DBNColoredTabs extends JBEditorTabs implements StatefulDisposable {
    private boolean disposed;
    private final Map<TabInfo, String> tabInfos = ContainerUtil.createConcurrentWeakMap();

    public DBNColoredTabs(@NotNull DBNForm form) {
        super(form.ensureProject(), null, form);
        setTabDraggingEnabled(true);
        Disposer.register(form, () -> Dispatch.run(() -> disposeInner()));
    }

    public void select(JComponent component, boolean requestFocus) {
        TabInfo tabInfo = findInfo(component);
        if (tabInfo != null) {
            selectTab(tabInfo.getText());
        }
    }

    @NotNull
    @Override
    public TabInfo addTab(TabInfo info, int index) {
        acknowledgeTab(info);
        return super.addTab(info, index);
    }

    @Override
    public TabInfo addTabSilently(TabInfo info, int index) {
        acknowledgeTab(info);
        return super.addTabSilently(info, index);
    }

    @NotNull
    @Override
    public TabInfo addTab(TabInfo info) {
        acknowledgeTab(info);
        return super.addTab(info);
    }

    private void acknowledgeTab(TabInfo info) {
        checkDisposed();
        tabInfos.put(info, info.getText());
    }

    @NotNull
    @Override
    public ActionCallback removeTab(TabInfo tabInfo) {
        return removeTab(tabInfo, true);
    }

    public ActionCallback removeTab(TabInfo info, boolean disposeComponent) {
        tabInfos.remove(info);
        Object object = info.getObject();
        ActionCallback actionCallback = super.removeTab(info);
        if (disposeComponent) {
            Disposer.dispose(object);
            info.setObject(null);
        }
        return actionCallback;
    }

    public void selectTab(String tabName) {
        if (tabName == null) return;

        for (TabInfo tabInfo : getTabs()) {
            if (Objects.equals(tabInfo.getText(), tabName)) {
                select(tabInfo, false);
                return;
            }
        }
    }

    public String getSelectedTabName() {
        TabInfo selectedInfo = getSelectedInfo();
        if (selectedInfo == null) return null;

        return selectedInfo.getText();
    }

    @Workaround //
    public void disposeInner() {
        if (disposed) return;
        disposed = true;

        for (TabInfo tabInfo : tabInfos.keySet()) {
            Object object = tabInfo.getObject();
            tabInfo.setObject(null);
            Disposer.dispose(object);
        }
        nullify();
    }
}
