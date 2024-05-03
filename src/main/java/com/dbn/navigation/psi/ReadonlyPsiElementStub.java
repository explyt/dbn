package com.dbn.navigation.psi;

import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public interface ReadonlyPsiElementStub extends NamedPsiElementStub {

    @Override
    default PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        throw notSupported();
    }

    @Override
    default PsiElement add(@NotNull PsiElement element) throws IncorrectOperationException {
        throw notSupported();
    }

    @Override
    default PsiElement addBefore(@NotNull PsiElement element, PsiElement anchor) throws IncorrectOperationException {
        throw notSupported();
    }

    @Override
    default PsiElement addAfter(@NotNull PsiElement element, PsiElement anchor) throws IncorrectOperationException {
        throw notSupported();
    }

    @Override
    default void checkAdd(@NotNull PsiElement element) throws IncorrectOperationException {
        throw notSupported();
    }

    @Override
    default PsiElement addRange(PsiElement first, PsiElement last) throws IncorrectOperationException {
        throw notSupported();
    }

    @Override
    default PsiElement addRangeBefore(@NotNull PsiElement first, @NotNull PsiElement last, PsiElement anchor) throws IncorrectOperationException {
        throw notSupported();
    }

    @Override
    default PsiElement addRangeAfter(PsiElement first, PsiElement last, PsiElement anchor) throws IncorrectOperationException {
        throw notSupported();
    }

    @Override
    default void delete() throws IncorrectOperationException {
        throw notSupported();
    }

    @Override
    default void checkDelete() throws IncorrectOperationException {
        throw notSupported();
    }

    @Override
    default void deleteChildRange(PsiElement first, PsiElement last) throws IncorrectOperationException {
        throw notSupported();
    }

    @Override
    default PsiElement replace(@NotNull PsiElement newElement) throws IncorrectOperationException {
        throw notSupported();
    }


    static @NotNull IncorrectOperationException notSupported() {
        return new IncorrectOperationException("Operation not supported");
    }
}
