package com.dbn.common.ui.util;

import lombok.experimental.UtilityClass;

import java.awt.*;

@UtilityClass
public class Cursors {
    public static Cursor defaultCursor() {
        return Cursor.getDefaultCursor();
    }

    public static Cursor handCursor() {
        return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    }

    public static Cursor textCursor() {
        return Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
    }

}
