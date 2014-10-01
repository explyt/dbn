package com.dci.intellij.dbn.common.options;

import javax.swing.Icon;
import javax.swing.JComponent;
import org.jdom.Element;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.util.Disposer;

public abstract class Configuration<T extends ConfigurationEditorForm> extends ConfigurationUtil implements SearchableConfigurable, PersistentConfiguration {
    public static ThreadLocal<Boolean> IS_RESETTING = new ThreadLocal<Boolean>();
    private T configurationEditorForm;
    private boolean isModified = false;

    public String getHelpTopic() {
        return null;
    }

    @Nls
    public String getDisplayName() {
        return null;
    }

    public Icon getIcon() {
        return null;
    }

    @NotNull
    public String getId() {
        return getClass().getName();
    }

    public Runnable enableSearch(String option) {
        return null;
    }

    @Nullable
    public final T getSettingsEditor() {
        return configurationEditorForm;
    }

    protected abstract T createConfigurationEditor();

    public JComponent createComponent() {
        configurationEditorForm = createConfigurationEditor();
        return configurationEditorForm == null ? null : configurationEditorForm.getComponent();
    }

    public void setModified(boolean modified) {
        if (modified && !isResetting()) {
            isModified = true;
        } else{
            isModified = modified;
        }
    }

    private Boolean isResetting() {
        Boolean isResetting = IS_RESETTING.get();
        return isResetting != null && isResetting;
    }

    public boolean isModified() {
        return isModified;
    }

    public void apply() throws ConfigurationException {
        if (configurationEditorForm != null && !configurationEditorForm.isDisposed()) {
            configurationEditorForm.applyFormChanges();
        }
        isModified = false;

        Configuration<T> settings = getOriginalSettings();
        if (settings != null && settings != this) {
            if (settings != this) {
                Element settingsElement = new Element("settings");
                writeConfiguration(settingsElement);
                settings.readConfiguration(settingsElement);
            }
        }
        onApply();
    }

    protected void onApply() {}

    protected Configuration<T> getOriginalSettings() {
        return null;
    }

    public void reset() {
        new ConditionalLaterInvocator() {
            @Override
            public void execute() {
                if (configurationEditorForm != null && !configurationEditorForm.isDisposed()) {
                    try {
                        IS_RESETTING.set(true);
                        configurationEditorForm.resetFormChanges();
                    } finally {
                        isModified = false;
                        IS_RESETTING.set(false);
                    }
                }

            }
        }.start();
    }

    public void disposeUIResources() {
        if (configurationEditorForm != null) {
            Disposer.dispose(configurationEditorForm);
            configurationEditorForm = null;
        }
    }

    public String getConfigElementName() {
        //throw new UnsupportedOperationException("Element name not defined for this configuration type.");
        return null;
    }
}
