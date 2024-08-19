package com.dbn.code.common.style.formatting;

import com.dbn.code.common.style.presets.CodeStylePreset;
import com.intellij.formatting.Wrap;
import org.jdom.Element;

import java.util.function.Supplier;

import static com.dbn.common.options.setting.Settings.enumAttribute;

public enum WrapDefinition implements FormattingAttribute<Wrap>{
    NONE    (() -> CodeStylePreset.WRAP_NONE),
    NORMAL  (() -> CodeStylePreset.WRAP_NORMAL),
    ALWAYS  (() -> CodeStylePreset.WRAP_ALWAYS),
    IF_LONG (() -> CodeStylePreset.WRAP_IF_LONG);

    private Wrap value;
    private Supplier<Wrap> loader;

    WrapDefinition(Supplier<Wrap> loader) {
        this.loader = loader;
    }

    @Override
    public Wrap getValue() {
        if (value == null && loader != null) {
            value = loader.get();
            loader = null;
        }
        return value;
    }

    public static WrapDefinition get(Element element) {
        return enumAttribute(element, "formatting-wrap", WrapDefinition.class);
    }
}
