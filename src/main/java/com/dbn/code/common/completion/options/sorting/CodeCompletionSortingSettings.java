package com.dbn.code.common.completion.options.sorting;

import com.dbn.code.common.completion.options.CodeCompletionSettings;
import com.dbn.code.common.completion.options.sorting.ui.CodeCompletionSortingSettingsForm;
import com.dbn.code.common.lookup.*;
import com.dbn.common.options.BasicConfiguration;
import com.dbn.language.common.TokenTypeCategory;
import com.dbn.object.common.DBObject;
import com.dbn.object.type.DBObjectType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.dbn.common.dispose.Checks.isValid;
import static com.dbn.common.options.setting.Settings.booleanAttribute;
import static com.dbn.common.options.setting.Settings.setBooleanAttribute;


@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class CodeCompletionSortingSettings extends BasicConfiguration<CodeCompletionSettings, CodeCompletionSortingSettingsForm> {
    private boolean enabled = true;
    private final List<CodeCompletionSortingItem> sortingItems = new ArrayList<>();

    public CodeCompletionSortingSettings(CodeCompletionSettings parent) {
        super(parent);
    }

    public int getSortingIndexFor(LookupItemBuilder lookupItemBuilder) {
        if (lookupItemBuilder instanceof VariableLookupItemBuilder) {
            return -2;
        }
        if (lookupItemBuilder instanceof AliasLookupItemBuilder) {
            return -1;
        }
        if (lookupItemBuilder instanceof ObjectLookupItemBuilder) {
            ObjectLookupItemBuilder objectLookupItemBuilder = (ObjectLookupItemBuilder) lookupItemBuilder;
            DBObject object = objectLookupItemBuilder.getObject();
            if (isValid(object)) {
                DBObjectType objectType = object.getObjectType();
                return getSortingIndexFor(objectType);
            }
        }

        if (lookupItemBuilder instanceof TokenLookupItemBuilder) {
            TokenLookupItemBuilder tokenLookupItemBuilder = (TokenLookupItemBuilder) lookupItemBuilder;
            TokenTypeCategory tokenTypeCategory = tokenLookupItemBuilder.getTokenTypeCategory();
            return getSortingIndexFor(tokenTypeCategory);
        }
        return 0;
    }

    public int getSortingIndexFor(DBObjectType objectType) {
        for (int i=0; i<sortingItems.size(); i++) {
            if (sortingItems.get(i).getObjectType() == objectType) {
                return sortingItems.size() - i;
            }
        }
        return 0;
    }

    public int getSortingIndexFor(TokenTypeCategory tokenTypeCategory) {
        for (int i=0; i<sortingItems.size(); i++) {
            if (sortingItems.get(i).getTokenTypeCategory() == tokenTypeCategory) {
                return sortingItems.size() - i;
            }
        }
        return 0;
    }

    @Override
    public String getDisplayName() {
        return txt("cfg.codeCompletion.title.Sorting");
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    @Override
    @NotNull
    public CodeCompletionSortingSettingsForm createConfigurationEditor() {
        return new CodeCompletionSortingSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "sorting";
    }

    @Override
    public void readConfiguration(Element element) {
        enabled = booleanAttribute(element, "enabled", enabled);
        for (Element child : element.getChildren()) {
            CodeCompletionSortingItem sortingItem = new CodeCompletionSortingItem(this);
            sortingItem.readConfiguration(child);
            sortingItems.remove(sortingItem);
            sortingItems.add(sortingItem);
        }
    }

    @Override
    public void writeConfiguration(Element element) {
        setBooleanAttribute(element, "enabled", enabled);
        for (CodeCompletionSortingItem sortingItem : sortingItems) {
            writeConfiguration(element, sortingItem);
        }
    }

}
