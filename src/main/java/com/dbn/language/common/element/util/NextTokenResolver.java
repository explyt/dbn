package com.dbn.language.common.element.util;

import com.dbn.common.util.Compactables;
import com.dbn.language.common.TokenType;
import com.dbn.language.common.element.ElementType;
import com.dbn.language.common.element.cache.ElementTypeLookupCache;
import com.dbn.language.common.element.impl.*;
import com.dbn.common.index.IndexContainer;
import com.dbn.common.util.Commons;
import com.dbn.language.common.element.impl.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public final class NextTokenResolver {
    private final ElementType source;
    private final Set<NamedElementType> visited = new HashSet<>();
    private IndexContainer<TokenType> bucket;

    private NextTokenResolver(ElementType source) {
        this.source = source;
    }

    public static NextTokenResolver from(ElementType source) {
        return new NextTokenResolver(source);
    }

    public IndexContainer<TokenType> resolve() {
        if (source instanceof NamedElementType) {
            visit((NamedElementType) source);
        } else {
            visitElement(source.getParent(), source);
        }
        Compactables.compact(bucket);
        return bucket;
    }

    private void visit(@NotNull NamedElementType element) {
        if (!visited.contains(element)) {
            visited.add(element);
            Set<ElementType> parents = element.getParents();
            for (ElementType parent : parents) {
                visitElement(parent, element);
            }
        }
    }

    private void visitElement(ElementType parent, ElementType child) {
        while (parent != null) {
            if (parent instanceof SequenceElementType) {
                parent = visitSequence((SequenceElementType) parent, child);

            } else if (parent instanceof IterationElementType) {
                visitIteration((IterationElementType) parent);
            }

            if (parent != null) {
                child = parent;
                parent = child.getParent();
                if (child instanceof NamedElementType) {
                    visit((NamedElementType) child);
                }
            }
        }
    }

    private void visitIteration(IterationElementType parent) {
        TokenElementType[] separatorTokens = parent.getSeparatorTokens();
        if (separatorTokens != null) {
            ensureBucket();
            for (TokenElementType separatorToken : separatorTokens) {
                bucket.add(separatorToken.getTokenType());
            }
        }
    }

    @Nullable
    private ElementType visitSequence(SequenceElementType parent, ElementType element) {
        int elementsCount = parent.getChildCount();
        int index = parent.indexOf(element, 0) + 1;

        if (index < elementsCount) {
            ElementTypeRef child = parent.getChild(index);
            while (child != null) {
                ensureBucket();
                ElementTypeLookupCache lookupCache = child.getLookupCache();
                lookupCache.captureFirstPossibleTokens(bucket);
                if (!child.isOptional()) {
                    parent = null;
                    break;
                }
                child = child.getNext();
            }
        }
        return parent;
    }

    private void ensureBucket() {
        bucket = Commons.nvl(bucket, () -> new IndexContainer<>());
    }
}
