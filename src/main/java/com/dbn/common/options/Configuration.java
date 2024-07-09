package com.dbn.common.options;

import com.dbn.common.options.ui.ConfigurationEditorForm;
import com.dbn.nls.NlsSupport;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dbn.common.dispose.Failsafe.nd;
import static com.dbn.common.util.Unsafe.cast;

public interface Configuration<P extends Configuration, E extends ConfigurationEditorForm>
        extends SearchableConfigurable, PersistentConfiguration, NlsSupport {

    @Nullable
    P getParent();

    @Nullable
    default <T> T getParentOfType(Class<T> type) {
        P parent = getParent();
        if (parent == null) return null;
        if (type.isAssignableFrom(parent.getClass())) return cast(parent);

        return cast(parent.getParentOfType(type));
    }

    @NotNull
    default P ensureParent() {
        return nd(getParent());
    }

    String getConfigElementName();

    @NotNull
    E createConfigurationEditor();

    E getSettingsEditor();

    E ensureSettingsEditor();

    Project resolveProject();
}
