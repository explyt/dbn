package com.dbn.common.options;

import com.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dbn.common.ui.util.UserInterface;
import com.dbn.options.TopLevelConfig;
import com.intellij.openapi.options.ConfigurationException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jdom.Element;

@EqualsAndHashCode(callSuper = false)
public abstract class CompositeConfiguration<P extends Configuration, E extends CompositeConfigurationEditorForm>
        extends BasicConfiguration<P, E> {

    private final @Getter(lazy = true) Configuration[] configurations = createConfigurations();

    public CompositeConfiguration(P parent) {
        super(parent);
    }

    protected abstract Configuration[] createConfigurations();

    @Override
    public final boolean isModified() {
        for (Configuration configuration : getConfigurations()) {
            if (configuration.isModified()) return true;
        }
        return super.isModified();
    }

    @Override
    public void apply() throws ConfigurationException {
        E settingsEditor = getSettingsEditor();
        if (this instanceof TopLevelConfig && settingsEditor != null) {
            UserInterface.stopTableCellEditing(settingsEditor.getComponent());
        }
        for (Configuration configuration : getConfigurations()) {
            configuration.apply();
        }
        super.apply();
    }

    @Override
    public final void reset() {
        for (Configuration configuration : getConfigurations()) {
            configuration.reset();
        }
        super.reset();
    }

    @Override
    public final void disposeUIResources() {
        super.disposeUIResources();
        for (Configuration configuration : getConfigurations()) {
            configuration.disposeUIResources();
        }
    }

    @Override
    public void readConfiguration(Element element) {
        Configuration[] configurations = getConfigurations();
        for (Configuration configuration : configurations) {
            readConfiguration(element, configuration);
        }
    }

    @Override
    public void writeConfiguration(Element element) {
        Configuration[] configurations = getConfigurations();
        for (Configuration configuration : configurations) {
            writeConfiguration(element, configuration);
        }
    }
}
