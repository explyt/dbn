package com.dbn.execution.compiler.ui;

import com.dbn.common.icon.Icons;
import com.dbn.common.ui.dialog.DBNDialog;
import com.dbn.execution.compiler.CompileType;
import com.dbn.object.common.DBSchemaObject;
import com.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;

@Getter
public class CompilerTypeSelectionDialog extends DBNDialog<CompilerTypeSelectionForm> {
    private CompileType selection;
    private DBObjectRef<DBSchemaObject> object;

    public CompilerTypeSelectionDialog(Project project, @Nullable DBSchemaObject object) {
        super(project, "Compile type", true);
        setModal(true);
        setResizable(false);
        this.object = DBObjectRef.of(object);
        //setVerticalStretch(0);
        init();
    }

    @NotNull
    @Override
    protected CompilerTypeSelectionForm createForm() {
        DBSchemaObject object = DBObjectRef.get(this.object);
        return new CompilerTypeSelectionForm(this, object);
    }

    @Override
    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                new CompileKeep(),
                new CompileNormalAction(),
                new CompileDebugAction(),
                getCancelAction(),
                //getHelpAction()
        };
    }

    private class CompileKeep extends AbstractAction {
        private CompileKeep() {
            super("Keep current");
            //super("Keep current", Icons.OBEJCT_COMPILE_KEEP);
            makeDefaultAction(this);;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            selection = CompileType.KEEP;
            doOKAction();
        }
    }

    private class CompileNormalAction extends AbstractAction {
        private CompileNormalAction() {
            super("Normal", Icons.OBJECT_COMPILE);
            //putValue(DEFAULT_ACTION, Boolean.TRUE);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            selection = CompileType.NORMAL;
            doOKAction();
        }
    }

    private class CompileDebugAction extends AbstractAction {
        private CompileDebugAction() {
            super("Debug", Icons.OBJECT_COMPILE_DEBUG);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            selection = CompileType.DEBUG;
            doOKAction();
        }
    }
}
