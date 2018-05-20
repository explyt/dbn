package com.dci.intellij.dbn.editor.data.filter;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.data.sorting.SortingState;
import com.dci.intellij.dbn.editor.data.filter.ui.DatasetBasicFilterForm;
import com.dci.intellij.dbn.object.DBDataset;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class DatasetBasicFilter extends DatasetFilterImpl {
    private List<DatasetBasicFilterCondition> conditions = new ArrayList<DatasetBasicFilterCondition>();
    private ConditionJoinType joinType = ConditionJoinType.AND;


    public DatasetBasicFilter(DatasetFilterGroup parent, String name) {
        super(parent, name, DatasetFilterType.BASIC);
    }

    public void generateName() {
        if (!isCustomNamed()) {
            boolean addSeparator = false;
            StringBuilder buffer = new StringBuilder();
            for (DatasetBasicFilterCondition condition : conditions) {
                if (condition.isActive() && condition.getValue().trim().length() > 0) {
                    if (addSeparator) buffer.append(joinType == ConditionJoinType.AND ? " & " : " | ");
                    addSeparator = true;
                    buffer.append(condition.getValue());
                    if (buffer.length() > 40) {
                        buffer.setLength(40);
                        buffer.append("...");
                        break;
                    }
                }
            }

            String name =  buffer.length() > 0 ? buffer.toString() : getFilterGroup().createFilterName("Filter");
            setName(name);
        }
    }

    public void addCondition(String columnName, Object value, ConditionOperator operator) {
        DatasetBasicFilterCondition condition = new DatasetBasicFilterCondition(this, columnName, value, operator, true);
        addCondition(condition);
    }

    public void addCondition(DatasetBasicFilterCondition condition) {
        conditions.add(condition);
    }

    public List<DatasetBasicFilterCondition> getConditions() {
        return conditions;
    }

    public void setJoinType(ConditionJoinType joinType) {
        this.joinType = joinType;
    }

    public ConditionJoinType getJoinType() {
        return joinType;
    }

    public boolean containsConditionForColumn(String columnName) {
        for (DatasetBasicFilterCondition condition : conditions) {
            if (condition.getColumnName().equals(columnName)) {
                return true;
            }
        }
        return false;
    }

    public String getVolatileName() {
        ConfigurationEditorForm configurationEditorForm = getSettingsEditor();
        if (configurationEditorForm != null) {
            DatasetBasicFilterForm basicFilterForm = (DatasetBasicFilterForm) configurationEditorForm;
            return basicFilterForm.getFilterName();
        }
        return super.getDisplayName();
    }

    public boolean isIgnored() {
        return false;
    }

    public Icon getIcon() {
        return  isTemporary() ? (
                    getError() == null ?
                        Icons.DATASET_FILTER_BASIC_TEMP : 
                        Icons.DATASET_FILTER_BASIC_TEMP_ERR) :
                    getError() == null ?
                        Icons.DATASET_FILTER_BASIC :
                        Icons.DATASET_FILTER_BASIC_ERR;
    }

    public String createSelectStatement(DBDataset dataset, SortingState sortingState) {
        setError(null);
        StringBuilder buffer = new StringBuilder();
        DatasetFilterUtil.createSimpleSelectStatement(dataset, buffer);
        boolean initialized = false;
        for (DatasetBasicFilterCondition condition : conditions) {
            if (condition.isActive()) {
                if (!initialized) {
                    buffer.append(" where ");
                    initialized = true;
                } else {
                    switch (joinType) {
                        case AND: buffer.append(" and "); break;
                        case OR: buffer.append(" or "); break;
                    }
                }
                condition.appendConditionString(buffer, dataset);
            }
        }

        DatasetFilterUtil.addOrderByClause(dataset, buffer, sortingState);
        DatasetFilterUtil.addForUpdateClause(dataset, buffer);
        return buffer.toString();
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
   @NotNull
   public ConfigurationEditorForm createConfigurationEditor() {
       DBDataset dataset = FailsafeUtil.get(lookupDataset());
       return new DatasetBasicFilterForm(dataset, this);
   }

   public void readConfiguration(Element element) {
       super.readConfiguration(element);
       joinType = SettingsUtil.getEnumAttribute(element, "join-type", ConditionJoinType.class);
       for (Object object : element.getChildren()) {
           DatasetBasicFilterCondition condition = new DatasetBasicFilterCondition(this);
           Element conditionElement = (Element) object;
           condition.readConfiguration(conditionElement);
           conditions.add(condition);
       }
   }

    public void writeConfiguration(Element element) {
        super.writeConfiguration(element);
        element.setAttribute("type", "basic");
        element.setAttribute("join-type", joinType.name());
        for (DatasetBasicFilterCondition condition: conditions) {
            Element conditionElement = new Element("condition");
            element.addContent(conditionElement);
            condition.writeConfiguration(conditionElement);
        }
    }
}
