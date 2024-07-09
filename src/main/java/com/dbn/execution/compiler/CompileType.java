package com.dbn.execution.compiler;

import com.dbn.common.icon.Icons;
import com.dbn.common.option.InteractiveOption;
import com.dbn.nls.NlsResources;
import lombok.Getter;

import javax.swing.*;

import static com.dbn.nls.NlsResources.txt;

@Getter
public enum CompileType implements InteractiveOption {
    NORMAL(txt("cfg.compiler.const.CompileType_NORMAL"), Icons.OBJECT_COMPILE, true),
    DEBUG(txt("cfg.compiler.const.CompileType_DEBUG"), Icons.OBJECT_COMPILE_DEBUG, true),
    KEEP(txt("cfg.compiler.const.CompileType_KEEP"), null/*Icons.OBEJCT_COMPILE_KEEP*/, true),
    ASK(txt("cfg.compiler.const.CompileType_ASK"), null/*Icons.OBEJCT_COMPILE_ASK*/, false);

    private final String name;
    private final Icon icon;
    private final boolean persistable;

    CompileType(String name, Icon icon, boolean persistable) {
        this.name = name;
        this.icon = icon;
        this.persistable = persistable;
    }

    @Override
    public boolean isCancel() {
        return false;
    }

    @Override
    public boolean isAsk() {
        return this == ASK;
    }
}
