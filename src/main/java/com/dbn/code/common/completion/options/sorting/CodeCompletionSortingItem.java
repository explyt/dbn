package com.dbn.code.common.completion.options.sorting;

import com.dbn.common.options.BasicConfiguration;
import com.dbn.common.options.ui.ConfigurationEditorForm;
import com.dbn.language.common.TokenTypeCategory;
import com.dbn.object.type.DBObjectType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.dbn.common.options.setting.Settings.stringAttribute;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class CodeCompletionSortingItem extends BasicConfiguration<CodeCompletionSortingSettings, ConfigurationEditorForm> {
    private DBObjectType objectType;
    private TokenTypeCategory tokenTypeCategory = TokenTypeCategory.UNKNOWN;

    CodeCompletionSortingItem(CodeCompletionSortingSettings parent) {
        super(parent);
    }

    public DBObjectType getObjectType() {
        return objectType;
    }

    public TokenTypeCategory getTokenTypeCategory() {
        return tokenTypeCategory;
    }

    public String getTokenTypeName() {
        return tokenTypeCategory.getName();
    }

    public String toString() {
        return objectType == null ? tokenTypeCategory.getName() : objectType.getName();
    }

    /*********************************************************
     *                      Configuration                    *
     *********************************************************/
    @Override
    @NotNull
    public ConfigurationEditorForm createConfigurationEditor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getConfigElementName() {
        return "sorting-element";
    }

    @Override
    public void readConfiguration(Element element) {
        String sortingItemType = stringAttribute(element, "type");
        if (Objects.equals(sortingItemType, "OBJECT")) {
            String objectTypeName = stringAttribute(element, "id");
            objectType = DBObjectType.get(objectTypeName);
        } else {
            String tokenTypeName = stringAttribute(element, "id");
            tokenTypeCategory = TokenTypeCategory.getCategory(tokenTypeName);
        }
    }

    @Override
    public void writeConfiguration(Element element) {
        if (objectType != null) {
            element.setAttribute("type", "OBJECT");
            element.setAttribute("id", objectType.getName());
        } else {
            element.setAttribute("type", "RESERVED_WORD");
            element.setAttribute("id", tokenTypeCategory.getName());
        }
    }
}
