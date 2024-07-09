package com.dbn.common.ui.util;

import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.util.ui.UIUtil;
import lombok.experimental.UtilityClass;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public final class Fonts {

    public static final Font REGULAR = UIUtil.getLabelFont();
    public static final Font BOLD = new Font(REGULAR.getName(), Font.BOLD, REGULAR.getSize());
    public static final Map<Font, Map<Float, Font>> SIZE_DERIVATIONS = new ConcurrentHashMap<>();
    public static final Map<Font, Map<Integer, Font>> STYLE_DERIVATIONS = new ConcurrentHashMap<>();

    public static Font getLabelFont() {
        return REGULAR;
    }
    
    public static Font getEditorFont() {
        EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
        return new Font(scheme.getEditorFontName(), Font.PLAIN, getLabelFont().getSize());
    }
    
    public static Font deriveFont(Font font, float size) {
        Map<Float, Font> cache = SIZE_DERIVATIONS.computeIfAbsent(font, f -> new ConcurrentHashMap<>());
        return cache.computeIfAbsent(size, s -> font.deriveFont(s));
    }
    
    public static Font deriveFont(Font font, int style) {
        Map<Integer, Font> cache = STYLE_DERIVATIONS.computeIfAbsent(font, f -> new ConcurrentHashMap<>());
        return cache.computeIfAbsent(style, s -> font.deriveFont(s));
    }

    public static Font smaller(Font font, float delta) {
        return deriveFont(font, font.getSize() - delta);
    }

    public static Font bigger(Font font, float delta) {
        return deriveFont(font, font.getSize() + delta);
    }
}
