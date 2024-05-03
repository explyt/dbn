package com.dbn.data.editor.text.actions;

import com.dbn.common.action.BasicAction;
import com.dbn.data.editor.text.TextContentType;
import com.dbn.data.editor.text.ui.TextEditorForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class TextContentTypeSelectAction extends BasicAction {
    private final TextEditorForm editorForm;
    private final TextContentType contentType;

    public TextContentTypeSelectAction(TextEditorForm editorForm, TextContentType contentType) {
        super(contentType.getName(), null, contentType.getIcon());
        this.contentType = contentType;
        this.editorForm = editorForm;
    }

    public TextContentType getContentType() {
        return contentType;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        editorForm.setContentType(contentType);

    }
}
