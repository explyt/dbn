package com.dbn.language.common.psi;

import com.dbn.code.common.style.formatting.FormattingAttributes;
import com.dbn.code.common.style.formatting.FormattingProviderPsiElement;
import com.dbn.code.common.style.formatting.IndentDefinition;
import com.dbn.code.common.style.formatting.SpacingDefinition;
import com.dbn.common.icon.Icons;
import com.dbn.language.common.element.ChameleonElementType;
import com.intellij.extapi.psi.ASTDelegatePsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ChameleonPsiElement extends ASTDelegatePsiElement implements ExecutableBundlePsiElement, FormattingProviderPsiElement {
    public static final FormattingAttributes FORMATTING_ATTRIBUTES = new FormattingAttributes(null, IndentDefinition.ABSOLUTE_NONE, SpacingDefinition.MIN_ONE_LINE, null);

    public final ASTNode node;
    public final ChameleonElementType elementType;

    public ChameleonPsiElement(@NotNull ASTNode node, ChameleonElementType elementType) {
        this.node = node;
        this.elementType = elementType;
    }


    @Override
    public PsiElement getParent() {
        ASTNode parentNode = node.getTreeParent();
        return parentNode == null ? null : parentNode.getPsi();
    }

    @Override
    @NotNull
    public ASTNode getNode() {
        return node;
    }


    @Override
    public List<ExecutablePsiElement> getExecutablePsiElements() {
        List<ExecutablePsiElement> bucket = new ArrayList<>();
        collectExecutablePsiElements(bucket, this);
        return bucket;
    }

    private static void collectExecutablePsiElements(List<ExecutablePsiElement> bucket, PsiElement element) {
        PsiElement child = element.getFirstChild();
        while (child != null) {
            if (child instanceof ExecutablePsiElement) {
                ExecutablePsiElement executablePsiElement = (ExecutablePsiElement) child;
                bucket.add(executablePsiElement);
            } else {
                collectExecutablePsiElements(bucket, child);
            }
            child = child.getNextSibling();
        }
    }

    @Override
    public String toString() {
        return elementType.getName();
    }

    @Override
    public Icon getIcon(int flags) {
        return Icons.FILE_BLOCK_PSQL; // todo make this dynamic
    }

    @Override
    public FormattingAttributes getFormattingAttributes() {
        return FORMATTING_ATTRIBUTES;
    }

    @Override
    public FormattingAttributes getFormattingAttributesRecursive(boolean left) {
        return FORMATTING_ATTRIBUTES;
    }
}
