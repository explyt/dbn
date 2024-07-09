package com.dbn.execution.compiler;

import com.dbn.common.option.InteractiveOption;
import com.dbn.nls.NlsResources;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.dbn.nls.NlsResources.txt;

@Getter
@AllArgsConstructor
public enum CompileDependenciesOption implements InteractiveOption {
    YES(txt("cfg.compiler.const.DependenciesOption_YES"), true),
    NO(txt("cfg.compiler.const.DependenciesOption_NO"), true),
    ASK(txt("cfg.compiler.const.DependenciesOption_ASK"), false);

    private final String name;
    private final boolean persistable;

    @Override
    public boolean isCancel() {
        return false;
    }

    @Override
    public boolean isAsk() {
        return this == ASK;
    }
}
