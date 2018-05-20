package com.dci.intellij.dbn.object;

import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.object.common.DBObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface DBColumn extends DBObject {
    DBDataType getDataType();
    boolean isPrimaryKey();
    boolean isSinglePrimaryKey();
    boolean isForeignKey();
    boolean isUniqueKey();
    boolean isNullable();
    boolean isHidden();
    DBDataset getDataset();
    int getPosition();

    @Nullable
    DBColumn getForeignKeyColumn();
    List<DBColumn> getReferencingColumns();  // foreign key columns referencing to this
    List<DBIndex> getIndexes();
    List<DBConstraint> getConstraints();

    int getConstraintPosition(DBConstraint constraint);
    DBConstraint getConstraintForPosition(int position);


}

