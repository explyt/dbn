package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.common.content.DynamicContentImpl;
import com.dci.intellij.dbn.common.content.DynamicContentProperty;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.content.GroupedDynamicContent;
import com.dci.intellij.dbn.common.content.dependency.ContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoaderImpl;
import com.dci.intellij.dbn.common.range.Range;
import com.dci.intellij.dbn.common.util.Commons;
import com.dci.intellij.dbn.connection.DatabaseEntity;
import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.object.type.DBObjectRelationType;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.dci.intellij.dbn.common.util.Commons.nvl;

@Getter
class DBObjectRelationListImpl<T extends DBObjectRelation> extends DynamicContentImpl<T> implements DBObjectRelationList<T>{
    private final DBObjectRelationType relationType;

    public DBObjectRelationListImpl(
            @NotNull DBObjectRelationType type,
            @NotNull DatabaseEntity parent,
            ContentDependencyAdapter dependencyAdapter,
            DynamicContentProperty... properties) {
        super(parent, dependencyAdapter, properties);
        this.relationType = type;
        //DBObjectListLoaderRegistry.register(parent, type, loader);
    }

    @Override
    public DynamicContentLoader<T, DBObjectMetadata> getLoader() {
        DynamicContentType<?> parentContentType = getParentEntity().getDynamicContentType();
        return DynamicContentLoaderImpl.resolve(parentContentType, relationType);
    }

    @Override
    @NotNull
    public List<T> getObjectRelations() {
        return getAllElements();
    }

    @Override
    public DynamicContentType getContentType() {
        return relationType;
    }

    @NotNull
    @Override
    public String getName() {
        return
                relationType.getSourceType().getName() + " " +
                relationType.getTargetType().getListName();
    }

    public String toString() {
        return relationType + " - " + super.toString();
    }

    @Override
    public List<DBObjectRelation> getRelationBySourceName(String sourceName) {
        List<DBObjectRelation> objectRelations = new ArrayList<>();
        for (DBObjectRelation objectRelation : getAllElements()) {
            if (Objects.equals(objectRelation.getSourceObject().getName(), sourceName)) {
                objectRelations.add(objectRelation);
            }
        }
        return objectRelations;
    }

    @Override
    public List<DBObjectRelation> getRelationByTargetName(String targetName) {
        List<DBObjectRelation> objectRelations = new ArrayList<>();
        for (DBObjectRelation objectRelation : getAllElements()) {
            if (Objects.equals(objectRelation.getTargetObject().getName(), targetName)) {
                objectRelations.add(objectRelation);
            }
        }
        return objectRelations;
    }

    @Override
    protected void sortElements(List<T> elements) {
        elements.sort(null);
    }

    /*********************************************************
     *                   DynamicContent                      *
     *********************************************************/

    @NotNull
    @Override
    public Project getProject() {
        return getParentEntity().getProject();
    }

   @Override
   public String getContentDescription() {
        if (getParentEntity() instanceof DBObject) {
            DBObject object = getParentEntity();
            return getName() + " of " + object.getQualifiedNameWithType();
        }
       return getName() + " from " + this.getConnection().getName() ;
    }

    @Override
    public void notifyChangeListeners() {}

    public static class Grouped<T extends DBObjectRelation> extends DBObjectRelationListImpl<T> implements GroupedDynamicContent<T> {
        private Map<DBObjectRef, Range> ranges;

        Grouped(
                @NotNull DBObjectRelationType relationType,
                @NotNull DatabaseEntity parent,
                ContentDependencyAdapter dependencyAdapter,
                DynamicContentProperty... properties) {
            super(relationType, parent, dependencyAdapter, properties);
            set(DynamicContentProperty.GROUPED, true);
        }


        @Override
        protected void afterUpdate() {
            List<T> elements = getAllElements();
            if (!elements.isEmpty()) {
                Map<DBObjectRef, Range> ranges = new HashMap<>();

                DBObjectRef currentObject = null;
                int currentNameOffset = 0;
                for (int i = 0; i < elements.size(); i++) {
                    T objectRelation = elements.get(i);
                    DBObject sourceObject = objectRelation.getSourceObject();
                    DBObject object = Commons.nvl(sourceObject.getParentObject(), sourceObject);
                    currentObject = nvl(currentObject, object.ref());

                    if (!Objects.equals(currentObject, object.ref())) {
                        ranges.put(currentObject, new Range(currentNameOffset, i - 1));
                        currentObject = object.ref();
                        currentNameOffset = i;
                    }

                    if (i == elements.size() - 1) {
                        ranges.put(currentObject, new Range(currentNameOffset, i));
                    }
                }

                this.ranges = ranges;
            }
        }

        public List<T> getChildElements(DatabaseEntity entity) {
            List<T> elements = getAllElements();
            if (ranges != null && entity instanceof DBObject) {
                DBObject object = (DBObject) entity;
                Range range = ranges.get(object.ref());
                if (range != null) {
                    return elements.subList(range.getLeft(), range.getRight() + 1);
                }
            }
            return Collections.emptyList();
        }

        @Override
        public T getElement(String name, short overload) {
    /*        if (parentNameRanges != null) {
                for (Range range : parentNameRanges.values()) {
                    SearchAdapter<T> binary = getObjectType().isOverloadable() ?
                            binary(name, overload) :
                            binary(name);

                    T element = Search.binarySearch(elements, range.getLeft(), range.getRight(), binary);
                    if (element != null) {
                        return element;
                    }
                }
            }*/
            return null;
        }
    }
}
