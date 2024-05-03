package com.dbn.execution.common.ui;

import com.dbn.common.color.Colors;
import com.dbn.common.ui.util.Borders;
import com.dbn.common.util.Documents;
import com.dbn.common.util.Editors;
import com.dbn.common.util.Viewers;
import com.dbn.execution.ExecutionResult;
import com.dbn.language.sql.SQLFileType;
import com.dbn.language.sql.SQLLanguage;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.*;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

import static com.dbn.common.dispose.Failsafe.nn;

public class StatementViewerPopup implements Disposable {
    private final String resultName;
    private EditorEx viewer;

    public StatementViewerPopup(ExecutionResult executionResult) {
        this.resultName = executionResult.getName();
        Project project = executionResult.getProject();

        PsiFile previewFile = nn(executionResult.createPreviewFile());
        Document document = Documents.ensureDocument(previewFile);
        viewer = Viewers.createViewer(document, project, null, SQLFileType.INSTANCE);
        viewer.setEmbeddedIntoDialogWrapper(true);
        Editors.initEditorHighlighter(viewer, SQLLanguage.INSTANCE, executionResult.getConnection());
        viewer.setBackgroundColor(Colors.getEditorCaretRowBackground());

        JScrollPane viewerScrollPane = viewer.getScrollPane();
        viewerScrollPane.setViewportBorder(Borders.lineBorder(Colors.getEditorCaretRowBackground(), 4));
        viewerScrollPane.setBorder(null);


        EditorSettings settings = viewer.getSettings();
        settings.setFoldingOutlineShown(false);
        settings.setLineMarkerAreaShown(false);
        settings.setLineNumbersShown(false);
        settings.setVirtualSpace(false);
        settings.setDndEnabled(false);
        settings.setAdditionalLinesCount(2);
        settings.setRightMarginShown(false);

        //mainPanel.setBorder(new LineBorder(Color.BLACK, 1, false));
    }

    public void show(Component component) {
        JBPopup popup = createPopup();
        popup.showInScreenCoordinates(component,
                new Point(
                        (int) (component.getLocationOnScreen().getX() + component.getWidth() +8),
                        (int) component.getLocationOnScreen().getY()));
    }

    public void show(Component component, Point point) {
        JBPopup popup = createPopup();
        point.setLocation(
                point.getX() + component.getLocationOnScreen().getX() + 16,
                point.getY() + component.getLocationOnScreen().getY() + 16);

        popup.showInScreenCoordinates(component, point);
    }

    private JBPopup createPopup() {
        ComponentPopupBuilder popupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(viewer.getComponent(), viewer.getContentComponent());
        popupBuilder.setMovable(true);
        popupBuilder.setResizable(true);
        popupBuilder.setRequestFocus(true);
        popupBuilder.setTitle("<html>" + resultName + "</html>");
        JBPopup popup = popupBuilder.createPopup();

        Dimension dimension = Editors.calculatePreferredSize(viewer);
        //Dimension dimension = ((EditorImpl) viewer).getPreferredSize();
        dimension.setSize(Math.min(dimension.getWidth() + 20, 1000), Math.min(dimension.getHeight() + 70, 800) );
        popup.setSize(dimension);

        popup.addListener(new JBPopupAdapter() {
            @Override
            public void onClosed(@NotNull LightweightWindowEvent event) {
                dispose();
            }
        });
        return popup;
    }

    @Override
    public void dispose() {
        if (viewer != null) {
            Editors.releaseEditor(viewer);
            viewer = null;
        }
    }
}
