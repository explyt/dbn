package com.dbn.debugger;

import com.dbn.common.ui.Presentable;
import com.dbn.nls.NlsResources;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.dbn.diagnostics.Diagnostics.conditionallyLog;
import static com.dbn.nls.NlsResources.txt;

@Getter
@AllArgsConstructor
public enum DBDebuggerType implements Presentable {
    JDBC(txt("app.debugger.const.DBDebuggerType_JDBC")),
    JDWP(txt("app.debugger.const.DBDebuggerType_JDWP")),
    NONE(txt("app.debugger.const.DBDebuggerType_NONE"));

    private final String name;

    public boolean isDebug() {
        return this != NONE;
    }

    public boolean isSupported() {
        switch (this) {
            case JDWP: {
                try {
                    Class.forName("com.intellij.debugger.engine.JavaStackFrame");
                    Class.forName("com.intellij.debugger.PositionManagerFactory");
                    return true;
                } catch (ClassNotFoundException e) {
                    conditionallyLog(e);
                    return false;
                }
            }
            case JDBC: return true;
            case NONE: return true;
        }
        return false;
    }
}
