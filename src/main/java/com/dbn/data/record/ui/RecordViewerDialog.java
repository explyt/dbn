package com.dbn.data.record.ui;

import com.dbn.common.icon.Icons;
import com.dbn.common.ui.dialog.DBNDialog;
import com.dbn.data.record.DatasetRecord;
import com.dbn.editor.data.DatasetEditorManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;

public class RecordViewerDialog extends DBNDialog<RecordViewerForm> {
    private final DatasetRecord record;

    public RecordViewerDialog(Project project, DatasetRecord record) {
        super(project, "View record", true);
        this.record = record; 
        setModal(false);
        setResizable(true);
        renameAction(getCancelAction(), "Close");
        init();
    }

    @NotNull
    @Override
    protected RecordViewerForm createForm() {
        return new RecordViewerForm(this, record);
    }

    @Override
    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                new OpenInEditorAction(),
                getCancelAction(),
                getHelpAction()
        };
    }
    
    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    private class OpenInEditorAction extends AbstractAction {
        public OpenInEditorAction() {
            super("Open In Editor", Icons.OBJECT_EDIT_DATA);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DatasetEditorManager datasetEditorManager = DatasetEditorManager.getInstance(record.getDataset().getProject());
            datasetEditorManager.openDataEditor(record.getFilterInput());
            doCancelAction();
        }
    }
}
