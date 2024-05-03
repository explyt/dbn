package com.dbn.language.common.element.cache;

import com.dbn.language.common.SharedTokenTypeBundle;
import com.dbn.language.common.TokenTypeBundle;
import com.dbn.language.common.element.impl.IdentifierElementType;
import com.dbn.language.common.element.impl.LeafElementType;
import com.dbn.language.common.element.impl.WrappingDefinition;
import com.dbn.common.index.IndexContainer;
import com.dbn.common.latent.Latent;
import com.dbn.language.common.TokenType;
import com.dbn.language.common.element.ElementType;

import java.util.Set;

import static com.dbn.common.util.Compactables.compact;

public abstract class ElementTypeLookupCacheIndexed<T extends ElementType> extends ElementTypeLookupCache<T> {

    private final IndexContainer<LeafElementType> allPossibleLeafs = new IndexContainer<>();
    protected final IndexContainer<LeafElementType> firstPossibleLeafs = new IndexContainer<>();
    protected final IndexContainer<LeafElementType> firstRequiredLeafs = new IndexContainer<>();

    private final IndexContainer<TokenType> allPossibleTokens = new IndexContainer<>();
    private final IndexContainer<TokenType> firstPossibleTokens = new IndexContainer<>();
    private final IndexContainer<TokenType> firstRequiredTokens = new IndexContainer<>();
    private final Latent<Boolean> startsWithIdentifier = Latent.basic(() -> checkStartsWithIdentifier());

    ElementTypeLookupCacheIndexed(T elementType) {
        super(elementType);
        assert !elementType.isLeaf();
    }

    public void initialize() {
        super.initialize();
        compact(allPossibleLeafs);
        compact(firstPossibleLeafs);
        compact(firstRequiredLeafs);

        compact(allPossibleTokens);
        compact(firstPossibleTokens);
        compact(firstRequiredTokens);
    }

    @Override
    public boolean isFirstPossibleToken(TokenType tokenType) {
        return firstPossibleTokens.contains(tokenType);
    }

    @Override
    public boolean isFirstRequiredToken(TokenType tokenType) {
        return firstRequiredTokens.contains(tokenType);
    }

    @Override
    public boolean containsToken(TokenType tokenType) {
        return allPossibleTokens.contains(tokenType);
    }

    @Override
    public boolean containsLeaf(LeafElementType elementType) {
        return allPossibleLeafs.contains(elementType);
    }

    @Override
    public Set<TokenType> getFirstPossibleTokens() {
        return firstPossibleTokens.elements(index -> getParserTokenTypes().getTokenType(index));
    }

    @Override
    public Set<TokenType> getFirstRequiredTokens() {
        return firstRequiredTokens.elements(index -> getParserTokenTypes().getTokenType(index));
    }

    private TokenTypeBundle getParserTokenTypes() {
        return elementType.getLanguageDialect().getParserTokenTypes();
    }

    @Override
    public Set<LeafElementType> getFirstPossibleLeafs() {
        return firstPossibleLeafs.elements(index -> getElementTypeBundle().getElement(index));
    }
    @Override
    public Set<LeafElementType> getFirstRequiredLeafs() {
        return firstRequiredLeafs.elements(index -> getElementTypeBundle().getElement(index));
    }

    @Override
    public boolean isFirstPossibleLeaf(LeafElementType elementType) {
        return firstPossibleLeafs.contains(elementType);
    }

    @Override
    public boolean isFirstRequiredLeaf(LeafElementType elementType) {
        return firstRequiredLeafs.contains(elementType);
    }

    @Override
    public boolean couldStartWithLeaf(LeafElementType elementType) {
        return firstPossibleLeafs.contains(elementType);
    }

    @Override
    public boolean couldStartWithToken(TokenType tokenType) {
        return firstPossibleTokens.contains(tokenType);
    }

    @Override
    public boolean shouldStartWithLeaf(LeafElementType elementType) {
        return firstRequiredLeafs.contains(elementType);
    }

    @Override
    public void registerLeaf(LeafElementType leaf, ElementType source) {
        boolean initAllElements = initAllElements(leaf);
        boolean initAsFirstPossibleLeaf = initAsFirstPossibleLeaf(leaf, source);
        boolean initAsFirstRequiredLeaf = initAsFirstRequiredLeaf(leaf, source);

        // register first possible leafs
        ElementTypeLookupCache<?> lookupCache = leaf.getLookupCache();
        if (initAsFirstPossibleLeaf) {
            firstPossibleLeafs.add(leaf);
            lookupCache.captureFirstPossibleTokens(firstPossibleTokens);
        }

        // register first required leafs
        if (initAsFirstRequiredLeaf) {
            firstRequiredLeafs.add(leaf);
            lookupCache.captureFirstPossibleTokens(firstRequiredTokens);
        }

        if (initAllElements) {
            // register all possible leafs
            allPossibleLeafs.add(leaf);

            // register all possible tokens
            if (leaf instanceof IdentifierElementType) {
                SharedTokenTypeBundle sharedTokenTypes = getSharedTokenTypes();
                allPossibleTokens.add(sharedTokenTypes.getIdentifier());
                allPossibleTokens.add(sharedTokenTypes.getQuotedIdentifier());
            } else {
                allPossibleTokens.add(leaf.getTokenType());
            }
        }

        if (initAsFirstPossibleLeaf || initAsFirstRequiredLeaf || initAllElements) {
            // walk the tree up
            registerLeafInParent(leaf);
        }
    }

    abstract boolean initAsFirstPossibleLeaf(LeafElementType leaf, ElementType source);
    abstract boolean initAsFirstRequiredLeaf(LeafElementType leaf, ElementType source);
    private boolean initAllElements(LeafElementType leafElementType) {
        return leafElementType != elementType && !allPossibleLeafs.contains(leafElementType);
    }

    protected void registerLeafInParent(LeafElementType leaf) {
        super.registerLeaf(leaf, null);
    }

    @Override
    public boolean startsWithIdentifier() {
        return startsWithIdentifier.get();
    }

    protected abstract boolean checkStartsWithIdentifier();

    boolean isWrapperBeginLeaf(LeafElementType leaf) {
        WrappingDefinition wrapping = elementType.getWrapping();
        return wrapping != null && wrapping.getBeginElementType() == leaf;
    }
}
