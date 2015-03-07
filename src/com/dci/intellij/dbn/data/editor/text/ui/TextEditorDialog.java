package com.dci.intellij.dbn.data.editor.text.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.data.editor.text.TextEditorAdapter;
import com.dci.intellij.dbn.data.editor.ui.UserValueHolder;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Action;
import javax.swing.JComponent;
import java.sql.SQLException;

public class TextEditorDialog extends DBNDialog {
    private TextEditorForm mainForm;

    private TextEditorDialog(Project project, TextEditorAdapter textEditorAdapter) throws SQLException {
        super(project, getTitle(textEditorAdapter), true);
        UserValueHolder userValueHolder = textEditorAdapter.getUserValueHolder();
        mainForm = new TextEditorForm(this, documentListener, userValueHolder, textEditorAdapter);
        getCancelAction().putValue(Action.NAME, "Close");
        getOKAction().setEnabled(false);
        setModal(true);
        init();
    }

    @NotNull
    private static String getTitle(TextEditorAdapter textEditorAdapter) {
        UserValueHolder userValueHolder = textEditorAdapter.getUserValueHolder();
        DBDataType dataType = userValueHolder.getDataType();
        String dataTypeName = dataType == null ? "OBJECT" : dataType.getName();
        DBObjectType objectType = userValueHolder.getObjectType();
        return "Edit " + dataTypeName.toUpperCase() + " content (" +objectType.getName().toLowerCase() + " " + userValueHolder.getName().toUpperCase() + ")";
    }

    protected String getDimensionServiceKey() {
        return "DBNavigator.LOBDataEditor";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return mainForm.getEditorComponent();
    }

    public static void show(Project project, TextEditorAdapter textEditorAdapter) {
        try {
            TextEditorDialog dialog = new TextEditorDialog(project, textEditorAdapter);
            dialog.show();
        } catch (SQLException e) {
            MessageUtil.showErrorDialog(project, "Could not load LOB content from database.", e);
        }
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                getOKAction(),
                getCancelAction(),
                getHelpAction()
        };
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
        try {
            mainForm.writeUserValue();
        } catch (SQLException e) {
            MessageUtil.showErrorDialog(getProject(), "Could not write LOB content to database.", e);
        }
    }

    @Nullable
    protected JComponent createCenterPanel() {
        return mainForm.getComponent();
    }

    DocumentListener documentListener = new DocumentAdapter() {
        @Override
        public void documentChanged(DocumentEvent event) {
            getCancelAction().putValue(Action.NAME, "Cancel");
            getOKAction().setEnabled(true);
        }    };

    @Override
    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            mainForm.dispose();
        }
    }
}
