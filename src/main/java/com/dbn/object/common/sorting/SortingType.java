package com.dbn.object.common.sorting;

import com.dbn.common.ui.Presentable;
import com.dbn.nls.NlsResources;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.dbn.nls.NlsResources.txt;

@Getter
@AllArgsConstructor
public enum SortingType implements Presentable{
    NAME(txt("app.objects.const.SortingType_NAME")),
    POSITION(txt("app.objects.const.SortingType_POSITION"));

    private final String name;

    @Override
    public String toString() {
        return name;
    }
}
