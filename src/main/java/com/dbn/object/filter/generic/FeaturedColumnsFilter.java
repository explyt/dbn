package com.dbn.object.filter.generic;

import com.dbn.common.filter.Filter;
import com.dbn.object.DBColumn;
import org.jetbrains.annotations.Nullable;


public class FeaturedColumnsFilter implements Filter<DBColumn> {
    public static final FeaturedColumnsFilter INSTANCE = new FeaturedColumnsFilter();

    private FeaturedColumnsFilter() {}

    @Nullable
    public static Filter<DBColumn> get(boolean pseudo, boolean audit) {
        if (pseudo && audit) return INSTANCE;
        if (pseudo) return NonPseudoColumnsFilter.INSTANCE;
        if (audit) return NonAuditColumnsFilter.INSTANCE;
        return null;
    }

    @Override
    public boolean accepts(DBColumn object) {
        return NonPseudoColumnsFilter.INSTANCE.accepts(object)
                && NonAuditColumnsFilter.INSTANCE.accepts(object);
    }
}
