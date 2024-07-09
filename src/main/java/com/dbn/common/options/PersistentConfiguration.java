package com.dbn.common.options;

import com.dbn.nls.NlsSupport;
import com.intellij.openapi.options.ConfigurationException;
import org.jdom.Element;

public interface PersistentConfiguration extends NlsSupport {
    void readConfiguration(Element element);
    void writeConfiguration(Element element);

    default void validate() throws ConfigurationException {};

    default void applyTo(PersistentConfiguration configuration) {
        Element element = new Element("configuration");
        writeConfiguration(element);
        configuration.readConfiguration(element);
    }
}