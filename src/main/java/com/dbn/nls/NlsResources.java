package com.dbn.nls;

import com.dbn.common.util.Localization;
import com.dbn.common.util.Named;
import com.intellij.DynamicBundle;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.io.File;

import static com.dbn.common.util.Commons.nvl;

public class NlsResources extends DynamicBundle{
    public static final @NonNls String BUNDLE = "messages.DBNResources";
    private static final NlsResources INSTANCE = new NlsResources();
    private static final Object[] EMPTY_PARAMS = new Object[0];

    static { Localization.initDefaultLocale(); }

    public NlsResources() {
        super(BUNDLE);
    }

    public static @Nls String txt(@PropertyKey(resourceBundle = BUNDLE) String key) {
        return INSTANCE.getMessage(key, EMPTY_PARAMS);
    }

    public static @Nls String txt(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        adjustParams(params);
        return INSTANCE.getMessage(key, params);
    }

    private static void adjustParams(Object ... params) {
        if (params == null || params.length == 0) return;
        for (int i = 0; i < params.length; i++) {
            if (params[i] instanceof Exception) {
                Exception exception = (Exception) params[i];
                params[i] = nvl(
                        exception.getLocalizedMessage(),
                        exception.getClass().getSimpleName());
            } else if (params[i] instanceof Named) {
                Named named = (Named) params[i];
                params[i] = named.getName();
            } else if (params[i] instanceof File) {
                File file = (File) params[i];
                params[i] = file.getPath();
            } else if (params[i] instanceof VirtualFile) {
                VirtualFile file = (VirtualFile) params[i];
                params[i] = file.getPath();
            }
        }
    }
}
