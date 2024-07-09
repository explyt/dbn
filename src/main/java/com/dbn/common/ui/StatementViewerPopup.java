package com.dbn.common.ui;

import com.dbn.common.color.Colors;
import com.dbn.common.ui.util.Borders;
import com.dbn.common.util.Context;
import com.dbn.common.util.Documents;
import com.dbn.common.util.Editors;
import com.dbn.common.util.Viewers;
import com.dbn.connection.ConnectionHandler;
import com.dbn.execution.ExecutionResult;
import com.dbn.language.common.DBLanguage;
import com.dbn.language.common.DBLanguagePsiFile;
import com.dbn.language.sql.SQLFileType;
import com.dbn.language.sql.SQLLanguage;
import com.intellij.lang.Language;
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
    private final String title;
    private EditorEx viewer;

    public StatementViewerPopup(String title, DBLanguagePsiFile previewFile, ConnectionHandler connection) {
        this.title = title;
        Project project = previewFile.getProject();
        DBLanguage language = DBLanguage.unwrap(previewFile.getLanguage());

        Document document = Documents.ensureDocument(previewFile);
        viewer = Viewers.createViewer(document, project, null, SQLFileType.INSTANCE);
        viewer.setEmbeddedIntoDialogWrapper(true);
        viewer.setBackgroundColor(Colors.getReadonlyEditorBackground());
        Editors.initEditorHighlighter(viewer, language, connection);

        JScrollPane viewerScrollPane = viewer.getScrollPane();
        viewerScrollPane.setViewportBorder(Borders.lineBorder(Colors.getReadonlyEditorBackground(), 8));
        viewerScrollPane.setBorder(null);


        EditorSettings settings = viewer.getSettings();
        settings.setFoldingOutlineShown(false);
        settings.setLineMarkerAreaShown(false);
        settings.setLineNumbersShown(false);
        settings.setVirtualSpace(false);
        settings.setDndEnabled(false);
        settings.setAdditionalLinesCount(2);
        settings.setRightMarginShown(false);
        settings.setCaretRowShown(false);

        //mainPanel.setBorder(new LineBorder(Color.BLACK, 1, false));
    }

    public void show(Component component) {
        JBPopup popup = createPopup();
        Point point = new Point(
                (int) (component.getLocationOnScreen().getX() + component.getWidth() + 8),
                (int) component.getLocationOnScreen().getY());
        popup.showInScreenCoordinates(component, point);
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
        popupBuilder.setTitle(title == null ? null : "<html>" + title + "</html>");
        JBPopup popup = popupBuilder.createPopup();

        Dimension dimension = Editors.calculatePreferredSize(viewer);
        dimension = new Dimension(
                (int) Math.min(dimension.getWidth() + 20, 1000),
                (int) Math.min(dimension.getHeight() + 60, 800));
        viewer.getScrollPane().setPreferredSize(dimension);

        popup.addListener(new JBPopupListener() {
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
