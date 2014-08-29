package com.dci.intellij.dbn.language.common.element.impl;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.jdom.Element;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.OneOfElementType;
import com.dci.intellij.dbn.language.common.element.lookup.OneOfElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.Branch;
import com.dci.intellij.dbn.language.common.element.parser.impl.OneOfElementTypeParser;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeDefinitionException;
import com.dci.intellij.dbn.language.common.psi.SequencePsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import gnu.trove.THashSet;

public class OneOfElementTypeImpl extends AbstractElementType implements OneOfElementType {
    protected final ElementTypeRef[] children;
    private boolean sortable;
    private Set<Branch> checkedBranches;

    public OneOfElementTypeImpl(ElementTypeBundle bundle, ElementType parent, String id, Element def) throws ElementTypeDefinitionException {
        super(bundle, parent, id, def);
        List children = def.getChildren();

        this.children = new ElementTypeRef[children.size()];

        for (int i=0; i<children.size(); i++) {
            Element child = (Element) children.get(i);
            String type = child.getName();
            ElementType elementType = bundle.resolveElementDefinition(child, type, this);
            double version = Double.parseDouble(CommonUtil.nvl(child.getAttributeValue("version"), "0"));
            Set<Branch> supportedBranches = parseBranchDefinitions(child.getAttributeValue("branch-check"));
            if (supportedBranches != null) {
                if (checkedBranches == null) {
                    checkedBranches = new THashSet<Branch>();
                }
                checkedBranches.addAll(supportedBranches);
            }
            this.children[i] = new ElementTypeRef(this, elementType, false, version, supportedBranches);
        }
        sortable = Boolean.parseBoolean(def.getAttributeValue("sortable"));
    }

    @Override
    protected OneOfElementTypeLookupCache createLookupCache() {
        return new OneOfElementTypeLookupCache(this);
    }

    @Override
    protected OneOfElementTypeParser createParser() {
        return new OneOfElementTypeParser(this);
    }

    public boolean isLeaf() {
        return false;
    }

    @Override
    public Set<Branch> getCheckedBranches() {
        return checkedBranches;
    }

    public String getDebugName() {
        return "one-of (" + getId() + ")";
    }

    public PsiElement createPsiElement(ASTNode astNode) {
        return new SequencePsiElement(astNode, this);
    }

    boolean sorted;
    public synchronized void sort() {
        if (sortable && ! sorted) {
            Arrays.sort(children, ONE_OF_COMPARATOR);
            sorted = true;
        }
    }

    private static final Comparator ONE_OF_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            ElementTypeRef et1 = (ElementTypeRef) o1;
            ElementTypeRef et2 = (ElementTypeRef) o2;

            int i1 = et1.getLookupCache().startsWithIdentifier() ? 1 : 2;
            int i2 = et2.getLookupCache().startsWithIdentifier() ? 1 : 2;
            return i2-i1;
        }
    };

    public ElementTypeRef[] getChildren() {
        return children;
    }
}
