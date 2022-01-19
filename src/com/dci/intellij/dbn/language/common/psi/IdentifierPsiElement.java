package com.dci.intellij.dbn.language.common.psi;

import com.dci.intellij.dbn.code.common.style.formatting.FormattingAttributes;
import com.dci.intellij.dbn.common.Capture;
import com.dci.intellij.dbn.common.consumer.ListCollector;
import com.dci.intellij.dbn.common.thread.ThreadMonitor;
import com.dci.intellij.dbn.common.util.RecursivityGate;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.language.common.QuotePair;
import com.dci.intellij.dbn.language.common.element.impl.IdentifierElementType;
import com.dci.intellij.dbn.language.common.element.impl.LeafElementType;
import com.dci.intellij.dbn.language.common.element.impl.QualifiedIdentifierVariant;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.element.util.IdentifierType;
import com.dci.intellij.dbn.language.common.psi.lookup.AliasDefinitionLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.IdentifierLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.LookupAdapterCache;
import com.dci.intellij.dbn.language.common.psi.lookup.ObjectDefinitionLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.PsiLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.VariableDefinitionLookupAdapter;
import com.dci.intellij.dbn.language.common.resolve.AliasObjectResolver;
import com.dci.intellij.dbn.language.common.resolve.SurroundingVirtualObjectResolver;
import com.dci.intellij.dbn.language.common.resolve.UnderlyingObjectResolver;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBSynonym;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBObjectPsiElement;
import com.dci.intellij.dbn.object.common.DBObjectPsiFacade;
import com.dci.intellij.dbn.object.common.DBVirtualObject;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.util.Set;
import java.util.function.Consumer;

import static com.dci.intellij.dbn.common.util.Commons.nvl;

public abstract class IdentifierPsiElement extends LeafPsiElement<IdentifierElementType> {
    private PsiResolveResult ref;
    private final RecursivityGate underlyingObjectResolver = new RecursivityGate(3);
    private final Capture<DBObject> underlyingObject = new Capture<>();

    public IdentifierPsiElement(ASTNode astNode, IdentifierElementType elementType) {
        super(astNode, elementType);
    }

    @Override
    public ItemPresentation getPresentation() {
        return this;
    }

    public boolean isQuoted() {
        CharSequence chars = getChars();
        if (chars.length() > 1) {
            // optimized lookup
            if (QuotePair.isPossibleBeginQuote(chars.charAt(0))) {
                return getIdentifierQuotes().isQuoted(chars);
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return getText();
    }

    @Override
    public FormattingAttributes getFormattingAttributes() {
        return super.getFormattingAttributes();
    }

    /**
     * ******************************************************
     * ItemPresentation                *
     * *******************************************************
     */
    @Override
    public String getPresentableText() {
        return Strings.toUpperCase(getUnquotedText()) + " (" + getObjectType() + ")";
    }

    @Override
    @Nullable
    public String getLocationString() {
        return null;
    }

    @Override
    @Nullable
    public Icon getIcon(boolean open) {
        DBObjectType type = getObjectType();
        return type.getIcon();
    }

    @Override
    @Nullable
    public TextAttributesKey getTextAttributesKey() {
        return null;
    }


    /**
     * ******************************************************
     * Lookup routines                 *
     * *******************************************************
     */
    @Override
    @Nullable
    public BasePsiElement findPsiElement(PsiLookupAdapter lookupAdapter, int scopeCrossCount) {
        if (lookupAdapter instanceof IdentifierLookupAdapter) {
            IdentifierLookupAdapter identifierLookupAdapter = (IdentifierLookupAdapter) lookupAdapter;
            if (identifierLookupAdapter.matchesName(this)) {
                /*PsiElement parentPsiElement = getParent();
                if (parentPsiElement instanceof QualifiedIdentifierPsiElement) {
                    QualifiedIdentifierPsiElement qualifiedIdentifierPsiElement = (QualifiedIdentifierPsiElement) parentPsiElement;
                    QualifiedIdentifierElementType qualifiedIdentifierElementType = qualifiedIdentifierPsiElement.getElementType();
                    if (!qualifiedIdentifierElementType.containsObjectType(identifierLookupAdapter.getObjectType())) {
                        return null;
                    }
                }*/
                return lookupAdapter.matches(this) ? this : null;
            }
        }
        return null;

    }

    @Override
    public void collectPsiElements(PsiLookupAdapter lookupAdapter, int scopeCrossCount, @NotNull Consumer<BasePsiElement> consumer) {
        if (lookupAdapter instanceof IdentifierLookupAdapter) {
            IdentifierLookupAdapter identifierLookupAdapter = (IdentifierLookupAdapter) lookupAdapter;
            if (identifierLookupAdapter.matchesName(this)) {
                if (lookupAdapter.matches(this)) {
                    consumer.accept(this);
                }
            }
        }
    }

    @Override
    public void collectSubjectPsiElements(@NotNull Consumer<IdentifierPsiElement> consumer) {
        if (getElementType().is(ElementTypeAttribute.SUBJECT)) {
            consumer.accept(this);
        }
    }

    @Override
    public void collectExecVariablePsiElements(@NotNull Consumer<ExecVariablePsiElement> consumer) {
    }

    /**
     * ******************************************************
     * Miscellaneous                     *
     * *******************************************************
     */
    public boolean isObject() {
        return getElementType().isObject();
    }

    public boolean isAlias() {
        return getElementType().isAlias();
    }

    public boolean isVariable() {
        return getElementType().isVariable();
    }


    public boolean isDefinition() {
        return getElementType().isDefinition();
    }


    public boolean isSubject() {
        return getElementType().isSubject();
    }

    public boolean isReference() {
        return getElementType().isReference();
    }
    
    public boolean isReferenceable() {
        return getElementType().isReferenceable();
    }

    public boolean isObjectOfType(DBObjectType objectType) {
        return getElementType().isObjectOfType(objectType);
    }

    public boolean isLocalReference() {
        return getElementType().isLocalReference();
    }

    public boolean isQualifiedIdentifierMember() {
        return getParent() instanceof QualifiedIdentifierPsiElement;
    }

    @NotNull
    public DBObjectType getObjectType() {
        if (ref != null) {
            DBObjectType objectType = ref.getObjectType();
            if (objectType != DBObjectType.UNKNOWN) {
                return objectType;
            }
        }
        return getElementType().getObjectType();
    }

    public String getObjectTypeName() {
        return getElementType().getObjectTypeName();
    }

    /**
     * Looks-up whatever underlying database object may be referenced from this identifier.
     * - if this references to a synonym, the DBObject behind the synonym is returned.
     * - if this is an alias reference or definition, it returns the underlying DBObject of the aliased identifier.
     *
     * @return real underlying database object behind the identifier.
     */
    @Override
    @Nullable
    public DBObject getUnderlyingObject() {
        if (underlyingObject.get() == null || !underlyingObject.valid(getChars())) {
            underlyingObjectResolver.run(() -> this.underlyingObject.capture(
                    loadUnderlyingObject(),
                    getChars()));
        }
        return underlyingObject.get();
    }

    private DBObject loadUnderlyingObject() {
        UnderlyingObjectResolver underlyingObjectResolver = getElementType().getUnderlyingObjectResolver();
        if (underlyingObjectResolver != null) {
            DBObject underlyingObject = underlyingObjectResolver.resolve(this);
            return resolveActualObject(underlyingObject);
        }

        IdentifierPsiElement originalElement = (IdentifierPsiElement) getOriginalElement();
        PsiElement psiReferenceElement = originalElement.resolve();
        if (psiReferenceElement != null && psiReferenceElement != this) {
            if (psiReferenceElement instanceof DBObjectPsiElement) {
                DBObjectPsiElement underlyingObject = (DBObjectPsiElement) psiReferenceElement;
                DBObject object = underlyingObject.getObject();
                return object == null ? null : resolveActualObject(object.getUndisposedEntity());
            }

            if (psiReferenceElement instanceof IdentifierPsiElement) {
                IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) psiReferenceElement;
                return identifierPsiElement.getUnderlyingObject();
            }
        }

        if (isAlias() && isDefinition()) {
            DBObject underlyingObject = AliasObjectResolver.getInstance().resolve(this);
            return resolveActualObject(underlyingObject);
        }

        DBObject underlyingObject = SurroundingVirtualObjectResolver.getInstance().resolve(this);
        if (underlyingObject != null) {
            return underlyingObject;
        }
        return null;
    }

    private static DBObject resolveActualObject(@Nullable DBObject object) {
        while (object instanceof DBSynonym) {
            DBSynonym synonym = (DBSynonym) object;
            object = synonym.getUnderlyingObject();
            if (object == null) return synonym;
        }
        return object;
    }

    @Override
    public NamedPsiElement findNamedPsiElement(String id) {
        return null;
    }

    @Override
    public BasePsiElement findPsiElementBySubject(ElementTypeAttribute attribute, CharSequence subjectName, DBObjectType subjectType) {
        if (getElementType().is(attribute) && getElementType().is(ElementTypeAttribute.SUBJECT)) {
            if (subjectType == getObjectType() && Strings.equalsIgnoreCase(subjectName, this.getChars())) {
                return this;
            }
        }
        return null;
    }

    /********************************************************
     *                      Variant builders                *
     *******************************************************/

    private Object[] buildAliasRefVariants() {
        SequencePsiElement statement = (SequencePsiElement) findEnclosingPsiElement(ElementTypeAttribute.STATEMENT);
        BasePsiElement sourceScope = getEnclosingScopePsiElement();
        DBObjectType objectType = getObjectType();
        PsiLookupAdapter lookupAdapter = LookupAdapterCache.ALIAS_DEFINITION.get(objectType);
        ListCollector<BasePsiElement> consumer = ListCollector.basic();
        lookupAdapter.collectInScope(statement, consumer);

        return consumer.isEmpty() ? new Object[0] : consumer.elements().toArray();
    }

    /********************************************************
     *                      Rersolvers                      *
     ********************************************************/

    private void resolveWithinQualifiedIdentifierElement(QualifiedIdentifierPsiElement qualifiedIdentifier) {
        int index = qualifiedIdentifier.getIndexOf(this);

        BasePsiElement parentObjectElement = null;
        DBObject parentObject = null;
        if (index > 0) {
            IdentifierPsiElement parentElement = qualifiedIdentifier.getLeafAtIndex(index - 1);
            if (parentElement.resolve() != null) {
                parentObjectElement = parentElement.isObject() || parentElement.isVariable() ? parentElement : PsiUtil.resolveAliasedEntityElement(parentElement);
                parentObject = parentObjectElement != null ? parentElement.getUnderlyingObject() : null;
            } else {
                return;
            }
        }

        for (QualifiedIdentifierVariant parseVariant : qualifiedIdentifier.getParseVariants()) {
            LeafElementType leafElementType = parseVariant.getLeaf(index);

            if (leafElementType instanceof IdentifierElementType) {
                IdentifierElementType elementType = (IdentifierElementType) leafElementType;
                DBObjectType objectType = elementType.getObjectType();

                CharSequence refText = ref.getText();
                if (parentObject == null || parentObject == getFile().getUnderlyingObject()) {  // index == 0
                    if (elementType.isObject()) {
                        resolveWithScopeParentLookup(objectType, elementType);
                    } else if (elementType.isAlias()) {
                        PsiLookupAdapter lookupAdapter = new AliasDefinitionLookupAdapter(this, objectType, refText);
                        BasePsiElement referencedElement = lookupAdapter.findInParentScopeOf(this);
                        if (updateReference(null, elementType, referencedElement)) return;

                    } else if (elementType.isVariable()) {
                        PsiLookupAdapter lookupAdapter = new VariableDefinitionLookupAdapter(this, DBObjectType.ANY, refText);
                        BasePsiElement referencedElement = lookupAdapter.findInParentScopeOf(this);
                        if (updateReference(null, elementType, referencedElement)) return;

                    }
                } else { // index > 0
                    IdentifierElementType parentElementType = (IdentifierElementType) parseVariant.getLeaf(index - 1);
                    if (parentObject.isOfType(parentElementType.getObjectType())) {
                        DBObject referencedObject = parentObject.getChildObject(objectType, refText.toString(), false);
                        if (updateReference(parentObjectElement, elementType, referencedObject)) return;

                    }
                }
            }
        }
    }

    private void resolveWithScopeParentLookup(DBObjectType objectType, IdentifierElementType elementType) {
        CharSequence refText = ref.getText();
        if (isPrecededByDot()) {
            LeafPsiElement prevLeaf = getPrevLeaf();
            if (prevLeaf != null) {
                LeafPsiElement parentPsiElement = prevLeaf.getPrevLeaf();
                if (parentPsiElement != null) {
                    DBObject object = parentPsiElement.getUnderlyingObject();
                    if (object != null && object != getFile().getUnderlyingObject()) {
                        DBObject referencedObject = object.getChildObject(refText.toString(), (short) 0, false);
                        if (updateReference(null, elementType, referencedObject)) return;
                    }
                }
            }
        }

        if (elementType.isObject()) {
            ConnectionHandler activeConnection = ref.getConnectionHandler();

            if (!elementType.isDefinition()){
                PsiLookupAdapter lookupAdapter = new ObjectDefinitionLookupAdapter(this, objectType, refText);
                PsiElement referencedElement = lookupAdapter.findInParentScopeOf(this);
                if (updateReference(null, elementType, referencedElement)) return;
            }

            if (!elementType.isLocalReference() && activeConnection != null && !activeConnection.isVirtual()) {
                String objectName = refText.toString();
                Set<DBObject> parentObjects = identifyPotentialParentObjects(objectType, null, this, this);
                if (parentObjects != null && parentObjects.size() > 0) {
                    for (DBObject parentObject : parentObjects) {
                        DBObject referencedObject = parentObject.getChildObject(objectType, objectName, false);
                        if (updateReference(null, elementType, referencedObject)) return;
                    }
                }

                DBObjectBundle objectBundle = activeConnection.getObjectBundle();
                DBObject referencedObject = objectBundle.getObject(objectType, objectName, (short) 0);
                if (updateReference(null, elementType, referencedObject)) {
                    return;
                }

                DBSchema schema = getDatabaseSchema();
                if (schema != null && objectType.isSchemaObject()) {
                    referencedObject = schema.getChildObject(objectType, objectName, false);
                    if (updateReference(null, elementType, referencedObject)) return;
                }
            }

        } else if (elementType.isAlias()) {
            PsiLookupAdapter lookupAdapter = new AliasDefinitionLookupAdapter(this, objectType, refText);
            BasePsiElement referencedElement = lookupAdapter.findInParentScopeOf(this);
            updateReference(null, elementType, referencedElement);
        } else if (elementType.isVariable()) {
            if (elementType.isReference()) {
                PsiLookupAdapter lookupAdapter = new VariableDefinitionLookupAdapter(this, DBObjectType.ANY, refText);
                BasePsiElement referencedElement = lookupAdapter.findInParentScopeOf(this);
                updateReference(null, elementType, referencedElement);
            }
        }
    }

    public boolean isPrecededByDot() {
        LeafPsiElement prevLeaf = getPrevLeaf();
        if (prevLeaf instanceof TokenPsiElement) {
            TokenPsiElement tokenPsiElement = (TokenPsiElement) prevLeaf;
            return tokenPsiElement.getTokenType() == tokenPsiElement.getLanguage().getSharedTokenTypes().getChrDot();
        }
        return false;
    }

    private boolean updateReference(@Nullable BasePsiElement parent, IdentifierElementType elementType, DBObject referenceObject) {
        if (isValidReference(referenceObject)) {
            ref.setParent(parent);
            ref.setReferencedElement(DBObjectPsiFacade.asPsiElement(referenceObject));
            this.setElementType(elementType);
            return true;
        }
        return false;
    }

    private boolean updateReference(@Nullable BasePsiElement parent, IdentifierElementType elementType, PsiElement referencedElement) {
        if (isValidReference(referencedElement)) {
            ref.setParent(parent);
            ref.setReferencedElement(referencedElement);
            this.setElementType(elementType);
            return true;
        }
        return false;
    }

    private boolean isValidReference(DBObject referencedObject) {
        if (referencedObject instanceof DBVirtualObject) {
            DBVirtualObject object = (DBVirtualObject) referencedObject;
            BasePsiElement underlyingPsiElement = object.getUnderlyingPsiElement();
            if (underlyingPsiElement != null && underlyingPsiElement.containsPsiElement(this)) {
                return false;
            }
        }
        return referencedObject != null;
    }

    private boolean isValidReference(PsiElement referencedElement) {
        if (referencedElement != null && referencedElement != this) {
            // check if inside same scope
            if (referencedElement instanceof IdentifierPsiElement) {
                IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) referencedElement;
                if (identifierPsiElement.isReference() && identifierPsiElement.isReferenceable()) {
                    return identifierPsiElement.getEnclosingScopePsiElement() == getEnclosingScopePsiElement();
                }
            }
            return true;
        }
        return false;
    }

    @NotNull
    @Override
    public PsiElement getNavigationElement() {
        return super.getNavigationElement();
    }

    /*********************************************************
     *                       PsiReference                    *
     ********************************************************/

    @Override
    @Nullable
    public PsiElement resolve() {
        if (isResolving()) {
            return ref.getReferencedElement();
        }

        if (isDefinition() && (isAlias() || (isVariable() && !isSubject()))) {
            // alias definitions do not have references.
            // underlying object is determined on runtime
            return null;
        }

        ConnectionHandler connectionHandler = getConnectionHandler();
        if ((connectionHandler == null || connectionHandler.isVirtual()) && isObject() && isDefinition()) {
            return null;
        }

        ref = nvl(ref, () -> new PsiResolveResult(this));

        if (ThreadMonitor.isDispatchThread()) {
            return ref.getReferencedElement();
        }
        if (ref.isDirty()) {
            boolean cancelled = false;
            try {
                ref.preResolve(this);
                CharSequence text = ref.getText();
                if (text != null && text.length() > 0) {
                    if (getParent() instanceof QualifiedIdentifierPsiElement) {
                        QualifiedIdentifierPsiElement qualifiedIdentifier = (QualifiedIdentifierPsiElement) getParent();
                        resolveWithinQualifiedIdentifierElement(qualifiedIdentifier);
                    } else {
                        resolveWithScopeParentLookup(getObjectType(), getElementType());
                    }
                }
            } catch (ProcessCanceledException e){
                cancelled = true;
                throw e;
            } finally {
                ref.postResolve(cancelled);
            }
        }
        return ref.getReferencedElement();
    }

    public void resolveAs(DBObject object) {
        ref = nvl(ref, () -> new PsiResolveResult(this));
        try {
            ref.preResolve(this);
            ref.setReferencedElement(DBObjectPsiFacade.asPsiElement(object));
        } finally {
            ref.postResolve(false);
        }
    }

    @Override
    public boolean isReferenceTo(@NotNull PsiElement element) {
        return element != this && ref != null && element == ref.getReferencedElement();
    }

    public CharSequence getUnquotedText() {
        CharSequence text = getChars();
        if (isQuoted() && text.length() > 1) {
            return text.subSequence(1, text.length() - 1);
        }
        return text;
    }

    @Override
    public boolean textMatches(@NotNull CharSequence text) {
        CharSequence chars = getChars();
        if (isQuoted())  {
            return chars.length() == text.length() + 2 && Strings.indexOfIgnoreCase(chars, text, 0) == 1;
        } else {
            return Strings.equalsIgnoreCase(chars, text);
        }
    }

    @Override
    public boolean isSoft() {
        return isDefinition();
    }

    @Override
    public boolean hasErrors() {
        return false;
    }

    @Override
    public boolean matches(BasePsiElement basePsiElement, MatchType matchType) {
        if (basePsiElement instanceof IdentifierPsiElement) {
            IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) basePsiElement;
            return matchType == MatchType.SOFT || Strings.equalsIgnoreCase(identifierPsiElement.getChars(), getChars());
        }

        return false;
    }

    public boolean isResolved() {
        return ref != null && ref.getReferencedElement() != null;
    }

    public boolean isResolving() {
        return ref != null && ref.isResolving();
    }

    public int getResolveAttempts() {
        return ref == null ? 0 : ref.getResolveAttempts();
    }

    public IdentifierType getIdentifierType() {
        return getElementType().getIdentifierType();
    }

    public void findQualifiedUsages(Consumer<BasePsiElement> consumer) {
        BasePsiElement scopePsiElement = getEnclosingScopePsiElement();
        if (scopePsiElement != null) {
            IdentifierLookupAdapter identifierLookupAdapter = new IdentifierLookupAdapter(this, null, null, null, getChars());
            identifierLookupAdapter.collectInElement(scopePsiElement, basePsiElement -> {
                QualifiedIdentifierPsiElement qualifiedIdentifierPsiElement =
                        (QualifiedIdentifierPsiElement) basePsiElement.findEnclosingPsiElement(QualifiedIdentifierPsiElement.class);

                if (qualifiedIdentifierPsiElement != null && qualifiedIdentifierPsiElement.getElementsCount() > 1) {
                    consumer.accept(qualifiedIdentifierPsiElement);
                }
            });
        }
    }

    @Nullable
    public QualifiedIdentifierPsiElement getParentQualifiedIdentifier() {
        return findEnclosingPsiElement(QualifiedIdentifierPsiElement.class);
    }
}
