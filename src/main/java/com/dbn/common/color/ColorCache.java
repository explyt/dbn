package com.dbn.common.color;

import com.dbn.common.event.ApplicationEvents;
import com.dbn.common.latent.Latent;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import gnu.trove.map.hash.TIntObjectHashMap;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.function.Supplier;

public class ColorCache {
    private static final Latent<ColorCache> cache = Latent.basic(() -> new ColorCache());
    private final TIntObjectHashMap<Color> store = new TIntObjectHashMap<>();

    private ColorCache() {
        ApplicationEvents.subscribe(null, EditorColorsManager.TOPIC, scheme -> store.clear());
        UIManager.addPropertyChangeListener(evt -> {
            if (Objects.equals(evt.getPropertyName(), "lookAndFeel")) {
                store.clear();
            }
        });
    }

    public static Color cached(int index, Supplier<Color> supplier) {
        Color color = store().get(index);
        if (color == null) {
            color = supplier.get();
            store().put(index, color);
        }
        return color;
    }

    private static TIntObjectHashMap<Color> store() {
        return cache.get().store;
    }
}
