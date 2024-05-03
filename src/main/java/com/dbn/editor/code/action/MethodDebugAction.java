package com.dbn.editor.code.action;

import com.dbn.common.icon.Icons;
import com.dbn.connection.operation.options.OperationSettings;
import com.dbn.database.DatabaseFeature;
import com.dbn.debugger.DatabaseDebuggerManager;
import com.dbn.editor.code.SourceCodeEditor;
import com.dbn.execution.compiler.options.CompilerSettings;
import com.dbn.object.DBMethod;
import com.dbn.object.common.DBSchemaObject;
import com.dbn.object.type.DBObjectType;
import com.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dbn.common.dispose.Checks.isValid;

public class MethodDebugAction extends AbstractCodeEditorAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull SourceCodeEditor fileEditor, @NotNull DBSourceCodeVirtualFile sourceCodeFile) {
        DBMethod method = (DBMethod) sourceCodeFile.getObject();
        DatabaseDebuggerManager debuggerManager = DatabaseDebuggerManager.getInstance(project);
        debuggerManager.startMethodDebugger(method);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project, @Nullable SourceCodeEditor fileEditor, @Nullable DBSourceCodeVirtualFile sourceCodeFile) {
        Presentation presentation = e.getPresentation();
        boolean visible = false;
        if (isValid(sourceCodeFile)) {
            DBSchemaObject schemaObject = sourceCodeFile.getObject();
            if (schemaObject.getObjectType().matches(DBObjectType.METHOD) && DatabaseFeature.DEBUGGING.isSupported(schemaObject)) {
                visible = true;
            }
        }

        presentation.setVisible(visible);
        presentation.setText("Debug Method");
        presentation.setIcon(Icons.METHOD_EXECUTION_DEBUG);
    }

    private static CompilerSettings getCompilerSettings(Project project) {
        return OperationSettings.getInstance(project).getCompilerSettings();
    }
}
