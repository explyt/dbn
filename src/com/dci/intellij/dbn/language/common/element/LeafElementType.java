package com.dci.intellij.dbn.language.common.element;

import java.util.Set;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.lookup.ElementLookupContext;
import com.dci.intellij.dbn.language.common.element.path.PathNode;

public interface LeafElementType extends ElementType {
    void setTokenType(TokenType tokenType);

    TokenType getTokenType();

    void setOptional(boolean optional);

    boolean isOptional();

    void registerLeaf();

    boolean isIdentifier();

    boolean isSameAs(LeafElementType leaf);

    Set<LeafElementType> getNextPossibleLeafs(PathNode pathNode, ElementLookupContext context);
}
