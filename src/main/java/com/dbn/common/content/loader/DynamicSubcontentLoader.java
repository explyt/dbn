package com.dbn.common.content.loader;

import com.dbn.common.content.DynamicContent;
import com.dbn.common.content.DynamicContentElement;
import com.dbn.common.content.DynamicContentProperty;
import com.dbn.common.content.DynamicContentType;
import com.dbn.common.content.GroupedDynamicContent;
import com.dbn.common.content.dependency.ContentDependencyAdapter;
import com.dbn.common.content.dependency.SubcontentDependencyAdapter;
import com.dbn.common.thread.ThreadInfo;
import com.dbn.common.thread.ThreadProperty;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.DatabaseEntity;
import com.dbn.database.common.metadata.DBObjectMetadata;
import com.dbn.database.interfaces.DatabaseInterfaceQueue;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * This loader is to be used from building the elements of a dynamic content, based on a source content.
 * e.g. Constraints of a table are loaded from the complete actions of constraints of a Schema.
 */
@Getter
public class DynamicSubcontentLoader<T extends DynamicContentElement, M extends DBObjectMetadata>
        extends DynamicContentLoaderImpl<T, M>
        implements DynamicContentLoader<T, M> {

    private DynamicContentLoader<T, M> alternativeLoader;

    private DynamicSubcontentLoader(String identifier, @NotNull DynamicContentType parentContentType, @NotNull DynamicContentType contentType) {
        super(identifier, parentContentType, contentType, true);
    }

    public static <T extends DynamicContentElement, M extends DBObjectMetadata> DynamicSubcontentLoader<T, M> create(
            String identifier, @NotNull DynamicContentType parentContentType,
            @NotNull DynamicContentType contentType,
            @Nullable DynamicContentLoader<T, M> alternativeLoader) {

        DynamicSubcontentLoader<T, M> loader = new DynamicSubcontentLoader<>(identifier, parentContentType, contentType);
        loader.alternativeLoader = alternativeLoader;
        return loader;
    }

    @Override
    public void loadContent(DynamicContent<T> content) throws SQLException {
        ContentDependencyAdapter dependency = content.getDependencyAdapter();
        if (dependency instanceof SubcontentDependencyAdapter) {
            SubcontentDependencyAdapter subcontentDependency = (SubcontentDependencyAdapter) dependency;
            DynamicContent<T> sourceContent = subcontentDependency.getSourceContent();
            boolean useAlternativeLoader = useAlternativeLoader(subcontentDependency);

            if (useAlternativeLoader) {
                sourceContent.loadInBackground();
                alternativeLoader.loadContent(content);

            } else if (sourceContent instanceof GroupedDynamicContent) {
                GroupedDynamicContent<T> groupedContent = (GroupedDynamicContent<T>) sourceContent;
                DatabaseEntity parent = content.ensureParentEntity();
                List<T> list = groupedContent.getChildElements(parent);
                content.setElements(list);
                content.set(DynamicContentProperty.MASTER, false);
            } else {
                content.setElements(Collections.emptyList());
                content.set(DynamicContentProperty.MASTER, false);
            }
        }

    }

    private boolean useAlternativeLoader(SubcontentDependencyAdapter subcontentDependency) {
        if (alternativeLoader == null) return false;

        DynamicContent<T> sourceContent = subcontentDependency.getSourceContent();
        if (sourceContent.isReady()) return false;

        ConnectionHandler connection = sourceContent.getConnection();
        if (!canUseAlternativeLoader(connection)) return false;

        return true;
    }

    private boolean canUseAlternativeLoader(ConnectionHandler connection) {
        DatabaseInterfaceQueue interfaceQueue = connection.getInterfaceQueue();
        int maxActiveTasks = interfaceQueue.maxActiveTasks();
        int count = interfaceQueue.size() + interfaceQueue.counters().active();

        ThreadInfo info = ThreadInfo.current();
        if (info.isOneOf(ThreadProperty.EDITOR_LOAD, ThreadProperty.DEBUGGER_NAVIGATION)) {
            return true; // quick load when invoked from an opening editor
        }

        if (count > maxActiveTasks) {
            return false;
        } else {
            return true;
        }
    }}
