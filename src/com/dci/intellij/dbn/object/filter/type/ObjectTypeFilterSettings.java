package com.dci.intellij.dbn.object.filter.type;

import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.options.ProjectConfiguration;
import com.dci.intellij.dbn.common.options.setting.BooleanSetting;
import com.dci.intellij.dbn.common.util.LazyValue;
import com.dci.intellij.dbn.common.util.SimpleLazyValue;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.filter.type.ui.ObjectTypeFilterSettingsForm;
import com.intellij.openapi.project.Project;

public class ObjectTypeFilterSettings extends ProjectConfiguration<ObjectTypeFilterSettingsForm> {
    private LazyValue<List<ObjectTypeFilterSetting>> objectTypeFilterSettings = new SimpleLazyValue<List<ObjectTypeFilterSetting>>() {
        @Override
        protected List<ObjectTypeFilterSetting> load() {
            List<ObjectTypeFilterSetting> objectTypeFilterSettings = new ArrayList<ObjectTypeFilterSetting>();
            objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.SCHEMA));
            objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.USER));
            objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.ROLE));
            objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.PRIVILEGE));
            objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.CHARSET));
            objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.TABLE));
            objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.VIEW));
            objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.MATERIALIZED_VIEW));
            objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.NESTED_TABLE));
            objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.COLUMN));
            objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.INDEX));
            objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.CONSTRAINT));
            objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.DATASET_TRIGGER));
            objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.DATABASE_TRIGGER));
            objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.SYNONYM));
            objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.SEQUENCE));
            objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.PROCEDURE));
            objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.FUNCTION));
            objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.PACKAGE));
            objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.TYPE));
            objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.TYPE_ATTRIBUTE));
            objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.ARGUMENT));
            objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.DIMENSION));
            objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.CLUSTER));
            objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.DBLINK));
            return objectTypeFilterSettings;
        }
    };
    private BooleanSetting useMasterSettings = new BooleanSetting("use-master-settings", true);
    private boolean projectLevel;

    public ObjectTypeFilterSettings(Project project, boolean projectLevel) {
        super(project);
        this.projectLevel = projectLevel;
    }

    public ObjectTypeFilterSettings getMasterSettings() {
        if (projectLevel) {
            return null;
        } else {
            DatabaseBrowserSettings databaseBrowserSettings = DatabaseBrowserSettings.getInstance(getProject());
            return databaseBrowserSettings.getFilterSettings().getObjectTypeFilterSettings();
        }
    }

    public BooleanSetting getUseMasterSettings() {
        return useMasterSettings;
    }

    @NotNull
    @Override
    public ObjectTypeFilterSettingsForm createConfigurationEditor() {
        return new ObjectTypeFilterSettingsForm(this);
    }

    public Filter<BrowserTreeNode> getElementFilter() {
        return elementFilter;
    }

    public Filter<DBObjectType> getTypeFilter() {
        return typeFilter;
    }

    private Filter<BrowserTreeNode> elementFilter = new Filter<BrowserTreeNode>() {
        public boolean accepts(BrowserTreeNode treeNode) {
            if (treeNode == null) {
                return false;
            }

            if (treeNode instanceof DBObject) {
                DBObject object = (DBObject) treeNode;
                DBObjectType objectType = object.getObjectType();
                return isVisible(objectType);
            }

            if (treeNode instanceof DBObjectList) {
                DBObjectList objectList = (DBObjectList) treeNode;
                return isVisible(objectList.getObjectType());
            }

            return true;
        }
    };

    private Filter<DBObjectType> typeFilter = new Filter<DBObjectType>() {
        public boolean accepts(DBObjectType objectType) {
            return objectType != null && isVisible(objectType);
        }
    };

    private boolean isVisible(DBObjectType objectType) {
        return projectLevel ?
            isSelected(objectType) :
            useMasterSettings.value() ?
                    getMasterSettings().isSelected(objectType) :
                    getMasterSettings().isSelected(objectType) && isSelected(objectType);
    }

    private boolean isSelected(DBObjectType objectType) {
        ObjectTypeFilterSetting objectTypeEntry = getObjectTypeEntry(objectType);
        return objectTypeEntry == null || objectTypeEntry.isSelected();
    }

    private void setVisible(DBObjectType objectType, boolean visible) {
        ObjectTypeFilterSetting objectTypeEntry = getObjectTypeEntry(objectType);
        if (objectTypeEntry != null) {
            objectTypeEntry.setSelected(visible);
        }
    }


    private ObjectTypeFilterSetting getObjectTypeEntry(DBObjectType objectType) {
        for (ObjectTypeFilterSetting objectTypeEntry : getSettings()) {
            DBObjectType visibleObjectType = objectTypeEntry.getObjectType();
            if (visibleObjectType == objectType || objectType.isInheriting(visibleObjectType)) {
                return objectTypeEntry;
            }
        }
        return null;
    }

    public List<ObjectTypeFilterSetting> getSettings() {
        return objectTypeFilterSettings.get();
    }

    public boolean isSelected(ObjectTypeFilterSetting objectFilterEntry) {
        for (ObjectTypeFilterSetting entry : getSettings()) {
            if (entry.equals(objectFilterEntry)) {
                return entry.isSelected();
            }
        }
        return false;
    }


    public String getConfigElementName() {
        return "object-type-filter";
    }

    public void readConfiguration(Element element) {
        useMasterSettings.readConfigurationAttribute(element);
        for (Object o : element.getChildren()) {
            Element child = (Element) o;
            String typeName = child.getAttributeValue("name");
            DBObjectType objectType = DBObjectType.getObjectType(typeName);
            if (objectType != null) {
                boolean enabled = Boolean.parseBoolean(child.getAttributeValue("enabled"));
                setVisible(objectType, enabled);
            }
        }
    }

    public void writeConfiguration(Element element) {
        if (!projectLevel) {
            useMasterSettings.writeConfigurationAttribute(element);
        }

        for (ObjectTypeFilterSetting objectTypeEntry : getSettings()) {
            Element child = new Element("object-type");
            child.setAttribute("name", objectTypeEntry.getName());
            child.setAttribute("enabled", Boolean.toString(objectTypeEntry.isSelected()));
            element.addContent(child);
        }
    }
}
