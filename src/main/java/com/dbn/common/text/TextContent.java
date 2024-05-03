package com.dbn.common.text;

import lombok.Data;

import java.util.Objects;

@Data
public class TextContent {
    public static TextContent EMPTY_PLAIN_TEXT = TextContent.plain("");

    private String text;
    private final MimeType type;

    public TextContent(String text, MimeType type) {
        this.text = text;
        this.type = type;
    }

    public TextContent replaceFields(String identifier, String replacement) {
        String text = this.text.replaceAll("\\$\\$" + identifier + "\\$\\$", replacement);
        if (Objects.equals(this.text, text)) return this;
        return new TextContent(text, type);
    }

    public String getTypeId() {
        return type.id();
    }

    public static TextContent plain(String text) {
        return new TextContent(text, MimeType.TEXT_PLAIN);
    }

    public static TextContent html(String text) {
        return new TextContent(text, MimeType.TEXT_HTML);
    }

    public static TextContent xml(String text) {
        return new TextContent(text, MimeType.TEXT_XML);
    }

    public static TextContent css(String text) {
        return new TextContent(text, MimeType.TEXT_CSS);
    }
}
