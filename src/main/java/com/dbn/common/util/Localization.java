package com.dbn.common.util;

import com.dbn.common.Reflection;
import com.intellij.DynamicBundle;
import lombok.experimental.UtilityClass;

import java.util.Locale;

@UtilityClass
public class Localization {
    public static Locale getLocale() {
        //return DynamicBundle.getLocale();
        return Unsafe.silent(null, () -> Reflection.invokeMethod(DynamicBundle.class, "getLocale"));
    }

    public static void initDefaultLocale() {
        Locale locale = getLocale();
        Locale systemLocale = Locale.getDefault();

        if (locale != null && !locale.equals(systemLocale)) {
            // TODO we sure?
            Locale.setDefault(locale);
        }
    }
}
