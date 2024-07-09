package com.dbn.editor.code.options;

import com.dbn.common.option.InteractiveOption;
import com.dbn.nls.NlsResources;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.dbn.nls.NlsResources.txt;

@Getter
@AllArgsConstructor
public enum CodeEditorChangesOption implements InteractiveOption {
    ASK(txt("cfg.codeEditor.const.ChangesOption_ASK")),
    SAVE(txt("cfg.codeEditor.const.ChangesOption_SAVE")),
    DISCARD(txt("cfg.codeEditor.const.ChangesOption_DISCARD")),
    SHOW(txt("cfg.codeEditor.const.ChangesOption_SHOW")),
    CANCEL(txt("cfg.codeEditor.const.ChangesOption_CANCEL"));

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
