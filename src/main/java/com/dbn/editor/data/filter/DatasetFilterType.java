package com.dbn.editor.data.filter;

import com.dbn.common.icon.Icons;
import com.dbn.common.ui.Presentable;
import com.dbn.nls.NlsResources;
import lombok.Getter;
import org.jetbrains.annotations.Nls;

import javax.swing.*;

import static com.dbn.nls.NlsResources.txt;

@Getter
public enum DatasetFilterType implements Presentable{
    NONE(txt("cfg.dataEditor.const.DatasetFilterType_NONE"), Icons.DATASET_FILTER_EMPTY, Icons.DATASET_FILTER_EMPTY),
    BASIC(txt("cfg.dataEditor.const.DatasetFilterType_BASIC"), Icons.DATASET_FILTER_BASIC, Icons.DATASET_FILTER_BASIC_ERR),
    CUSTOM(txt("cfg.dataEditor.const.DatasetFilterType_CUSTOM"), Icons.DATASET_FILTER_CUSTOM, Icons.DATASET_FILTER_CUSTOM_ERR),
    GLOBAL(txt("cfg.dataEditor.const.DatasetFilterType_GLOBAL"), Icons.DATASET_FILTER_GLOBAL, Icons.DATASET_FILTER_GLOBAL_ERR);

    private final String name;
    private final Icon icon;
    private final Icon errIcon;

    DatasetFilterType(@Nls String name, Icon icon, Icon errIcon) {
        this.name = name;
        this.icon = icon;
        this.errIcon = errIcon;
    }
}
