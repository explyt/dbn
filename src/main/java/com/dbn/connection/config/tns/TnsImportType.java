package com.dbn.connection.config.tns;

import com.dbn.common.text.TextContent;
import com.dbn.common.ui.Presentable;
import com.dbn.common.util.Commons;
import com.dbn.nls.NlsResources;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import static com.dbn.nls.NlsResources.txt;

@Getter
@AllArgsConstructor
public enum TnsImportType implements Presentable {
    FIELDS(txt("cfg.connection.const.TnsImportType_FIELDS"), loadInfo("tns_import_type_fields.html")),
    PROFILE(txt("cfg.connection.const.TnsImportType_PROFILE"), loadInfo("tns_import_type_profile.html")),
    DESCRIPTOR(txt("cfg.connection.const.TnsImportType_DESCRIPTOR"), loadInfo("tns_import_type_descriptor.html"));

    private final String name;
    private final TextContent info;

    @NotNull
    @SneakyThrows
    private static TextContent loadInfo(String fileName) {
        String content = Commons.readInputStream(TnsImportType.class.getResourceAsStream(fileName));
        return TextContent.html(content);
    }
}
