package com.dbn.debugger;

import com.dbn.common.dispose.Failsafe;
import com.dbn.editor.DatabaseFileEditorManager;
import com.dbn.editor.code.SourceCodeManager;
import com.dbn.object.DBMethod;
import com.dbn.object.common.DBSchemaObject;
import com.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XSourcePosition;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class DBDebugUtil {

    public static @Nullable DBSchemaObject getObject(@Nullable XSourcePosition sourcePosition) {
        if (sourcePosition == null) return null;

        VirtualFile virtualFile = sourcePosition.getFile();
        if (virtualFile instanceof DBEditableObjectVirtualFile) {
            DBEditableObjectVirtualFile databaseFile = (DBEditableObjectVirtualFile) virtualFile;
            return databaseFile.getObject();
        }

        if (virtualFile instanceof DBSourceCodeVirtualFile) {
            DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) virtualFile;
            return sourceCodeFile.getObject();
        }
        return null;
    }

    public static VirtualFile getSourceCodeFile(XSourcePosition sourcePosition) {
        if (sourcePosition == null) return null;

        return sourcePosition.getFile();
    }


    @Nullable
    public static DBEditableObjectVirtualFile getMainDatabaseFile(DBMethod method) {
        DBSchemaObject schemaObject = getMainDatabaseObject(method);
        return schemaObject == null ? null : (DBEditableObjectVirtualFile) schemaObject.getVirtualFile();
    }

    @Nullable
    public static DBSchemaObject getMainDatabaseObject(DBMethod method) {
        return method != null && method.isProgramMethod() ? method.getProgram() : method;
    }

    public static void openEditor(VirtualFile virtualFile) {
        if (virtualFile instanceof DBEditableObjectVirtualFile) {
            DBEditableObjectVirtualFile databaseFile = (DBEditableObjectVirtualFile) virtualFile;
            Project project = Failsafe.nn(databaseFile.getProject());
            SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
            sourceCodeManager.ensureSourcesLoaded(databaseFile.getObject(), false);

            DatabaseFileEditorManager editorManager = DatabaseFileEditorManager.getInstance(project);
            editorManager.connectAndOpenEditor(databaseFile.getObject(), null, false, false);
        } else if (virtualFile instanceof DBSourceCodeVirtualFile) {
            DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) virtualFile;
            DBEditableObjectVirtualFile mainDatabaseFile = sourceCodeFile.getMainDatabaseFile();
            openEditor(mainDatabaseFile);
        }
    }
}