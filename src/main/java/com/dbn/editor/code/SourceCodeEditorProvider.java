package com.dbn.editor.code;

import com.dbn.database.DatabaseFeature;
import com.dbn.editor.DBContentType;
import com.dbn.editor.EditorProviderId;
import com.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.dbn.database.DatabaseFeature.OBJECT_SOURCE_EDITING;
import static com.dbn.editor.DBContentType.CODE;
import static com.dbn.editor.DBContentType.CODE_AND_DATA;

public class SourceCodeEditorProvider extends SourceCodeEditorProviderBase {

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        if (virtualFile instanceof DBEditableObjectVirtualFile) {
            DBEditableObjectVirtualFile databaseFile = (DBEditableObjectVirtualFile) virtualFile;

            DBContentType contentType = databaseFile.getContentType();
            return contentType.isOneOf(CODE, CODE_AND_DATA) &&
                    OBJECT_SOURCE_EDITING.isSupported(databaseFile);

        }
        return super.accept(project, virtualFile);
    }


    @Override
    public DBContentType getContentType() {
        return CODE;
    }

    @Override
    @NotNull
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;

    }

    @NotNull
    @Override
    public EditorProviderId getEditorProviderId() {
        return EditorProviderId.CODE;
    }

    @Override
    public String getName() {
        return "Code";
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    /*********************************************************
     *                ApplicationComponent                   *
     *********************************************************/

    @Override
    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.DBSourceEditorProvider";
    }

}
