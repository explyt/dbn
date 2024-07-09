package com.dbn.code.common.style.options;

import com.dbn.common.ui.Presentable;
import com.dbn.nls.NlsResources;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.dbn.nls.NlsResources.txt;

@Getter
@AllArgsConstructor
public enum CodeStyleCase implements Presentable{
    PRESERVE (txt("cfg.codeStyle.const.CodeStyleCase_PRESERVE")),
    UPPER(txt("cfg.codeStyle.const.CodeStyleCase_UPPER")),
    LOWER(txt("cfg.codeStyle.const.CodeStyleCase_LOWER")),
    CAPITALIZED(txt("cfg.codeStyle.const.CodeStyleCase_CAPITALIZED"));

    private final String name;

    @Override
    public String toString() {
        return name;
    }
}
