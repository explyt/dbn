package com.dbn.execution;

import com.dbn.common.option.InteractiveOption;
import com.dbn.nls.NlsResources;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.dbn.nls.NlsResources.txt;

@Getter
@Deprecated
@AllArgsConstructor
public enum TargetConnectionOption implements InteractiveOption{
    ASK(txt("cfg.execution.const.TargetConnectionOption_ASK")),
    MAIN(txt("cfg.execution.const.TargetConnectionOption_MAIN")),
    POOL(txt("cfg.execution.const.TargetConnectionOption_POOL")),
    CANCEL(txt("cfg.execution.const.TargetConnectionOption_CANCEL"));

    private final String name;

    @Override
    public boolean isCancel() {
        return this == CANCEL;
    }

    @Override
    public boolean isAsk() {
        return this == ASK;
    }
}
