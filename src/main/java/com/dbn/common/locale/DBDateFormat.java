package com.dbn.common.locale;

import com.dbn.common.ui.Presentable;
import com.dbn.nls.NlsResources;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.text.DateFormat;

import static com.dbn.nls.NlsResources.txt;

@Getter
@AllArgsConstructor
public enum DBDateFormat implements Presentable {
    FULL(txt("cfg.shared.const.DateFormat_FULL"), DateFormat.FULL),
    SHORT(txt("cfg.shared.const.DateFormat_SHORT"), DateFormat.SHORT),
    MEDIUM(txt("cfg.shared.const.DateFormat_MEDIUM"), DateFormat.MEDIUM),
    LONG(txt("cfg.shared.const.DateFormat_LONG"), DateFormat.LONG),
    CUSTOM(txt("cfg.shared.const.DateFormat_CUSTOM"), 0);

    private final String name;
    private final int format;
}
