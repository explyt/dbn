package com.dbn.navigation.psi;

import com.dbn.common.compatibility.Compatibility;
import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ReadonlyPsiDirectoryStub extends PsiDirectory, ReadonlyPsiElementStub {

    @Override
    default boolean isDirectory() {
        return true;
    }

    default void checkSetName(String name) throws IncorrectOperationException {
        throw ReadonlyPsiElementStub.notSupported();
    }

    @Override
    default @NotNull PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        throw ReadonlyPsiElementStub.notSupported();
    }

    @Override
    @NotNull
    default PsiDirectory createSubdirectory(@NotNull String name) throws IncorrectOperationException {
        throw ReadonlyPsiElementStub.notSupported();
    }

    @Override
    default void checkCreateSubdirectory(@NotNull String name) throws IncorrectOperationException {
        throw ReadonlyPsiElementStub.notSupported();
    }

    @Override
    @NotNull
    default PsiFile createFile(@NotNull String name) throws IncorrectOperationException {
        throw ReadonlyPsiElementStub.notSupported();
    }

    @Override
    @NotNull
    default PsiFile copyFileFrom(@NotNull String newName, @NotNull PsiFile originalFile) throws IncorrectOperationException {
        throw ReadonlyPsiElementStub.notSupported();
    }

    @Override
    default void checkCreateFile(@NotNull String name) throws IncorrectOperationException {
        throw ReadonlyPsiElementStub.notSupported();
    }

    @Override
    default PsiFile getContainingFile() throws PsiInvalidElementAccessException {
        return null;
    }

    @Override
    default PsiDirectory getParentDirectory() {
        return getParent();
    }

    @NotNull
    @Override
    default PsiDirectory[] getSubdirectories() {
        return PsiDirectory.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    default PsiFile[] getFiles() {
        return PsiFile.EMPTY_ARRAY;
    }

    @Override
    default PsiDirectory findSubdirectory(@NotNull String name) {
        return null;
    }

    @Override
    default PsiFile findFile(@NotNull String name) {
        return null;
    }

    @Override
    @Compatibility
    default boolean processChildren(@NotNull PsiElementProcessor processor) {
        return false;
    }

    @Override
    @Compatibility
    default boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, @Nullable PsiElement lastParent, @NotNull PsiElement place) {
        return false;
    }
}
