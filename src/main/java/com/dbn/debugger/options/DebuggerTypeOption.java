package com.dbn.debugger.options;

import com.dbn.common.option.InteractiveOption;
import com.dbn.debugger.DBDebuggerType;
import com.dbn.nls.NlsResources;
import lombok.Getter;

import static com.dbn.nls.NlsResources.txt;

@Getter
public enum DebuggerTypeOption implements InteractiveOption {
    JDBC(txt("cfg.debugger.const.DebuggerTypeOption_JDBC"), DBDebuggerType.JDBC),
    JDWP(txt("cfg.debugger.const.DebuggerTypeOption_JDWP"), DBDebuggerType.JDWP),
    ASK(txt("cfg.debugger.const.DebuggerTypeOption_ASK")),
    CANCEL(txt("cfg.debugger.const.DebuggerTypeOption_CANCEL"));

    private final String name;
    private final DBDebuggerType debuggerType;

    DebuggerTypeOption(String name) {
        this.name = name;
        this.debuggerType = null;
    }

    DebuggerTypeOption(String name, DBDebuggerType debuggerType) {
        this.name = name;
        this.debuggerType = debuggerType;
    }

    @Override
    public boolean isCancel() {
        return this == CANCEL;
    }

    @Override
    public boolean isAsk() {
        return this == ASK;
    }
}
