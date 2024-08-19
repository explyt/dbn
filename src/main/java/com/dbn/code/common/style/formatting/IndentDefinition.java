package com.dbn.code.common.style.formatting;

import com.intellij.formatting.Indent;
import org.jdom.Element;

import java.util.function.Supplier;

import static com.dbn.common.options.setting.Settings.enumAttribute;

public enum IndentDefinition implements FormattingAttribute<Indent> {
    NONE          (() -> Indent.getNoneIndent()),
    NORMAL        (() -> Indent.getNormalIndent(true)),
    CONTINUE      (() -> Indent.getContinuationIndent()),
    ABSOLUTE_NONE (() -> Indent.getAbsoluteNoneIndent());

    private Indent value;
    private Supplier<Indent> loader;

    IndentDefinition(Supplier<Indent> loader) {
        this.loader = loader;
    }

    @Override
    public Indent getValue() {
        if (value == null && loader != null) {
            value = loader.get();
            loader = null;
        }
        return value;
    }

    public static IndentDefinition get(Element element) {
        return enumAttribute(element, "formatting-indent", IndentDefinition.NORMAL);
    }
}
