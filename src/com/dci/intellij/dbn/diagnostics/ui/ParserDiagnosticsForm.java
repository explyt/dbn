package com.dci.intellij.dbn.diagnostics.ui;

import com.dci.intellij.dbn.common.action.DataKeys;
import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.diagnostics.ParserDiagnosticsManager;
import com.dci.intellij.dbn.diagnostics.data.ParserDiagnosticsResult;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.GuiUtils;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.List;

public class ParserDiagnosticsForm extends DBNFormImpl {
    private JPanel mainPanel;
    private JPanel detailsPanel;
    private JList<ParserDiagnosticsResult> resultsList;

    private final ParserDiagnosticsDetailsForm detailsForm;
    private final ParserDiagnosticsManager manager;

    public ParserDiagnosticsForm(@NotNull ParserDiagnosticsToolWindowForm toolWindowForm) {
        super(toolWindowForm);
        manager = ParserDiagnosticsManager.getInstance(ensureProject());
        GuiUtils.replaceJSplitPaneWithIDEASplitter(mainPanel);
        GUIUtil.updateSplitterProportion(mainPanel, (float) 0.2);

        mainPanel.setBorder(Borders.BOTTOM_LINE_BORDER);

        detailsForm = new ParserDiagnosticsDetailsForm(this);
        detailsPanel.add(detailsForm.getComponent(), BorderLayout.CENTER);

        resultsList.addListSelectionListener(e -> {
            ParserDiagnosticsResult current = resultsList.getSelectedValue();
            if (current != null) {
                ParserDiagnosticsResult previous = manager.getPreviousResult(current);
                detailsForm.renderResult(previous, current);
            }
        });

        resultsList.setCellRenderer(new ResultListCellRenderer());
        refreshResults();
        selectResult(manager.getLatestResult());
    }

    public void refreshResults() {
        DefaultListModel<ParserDiagnosticsResult> model = new DefaultListModel<>();
        List<ParserDiagnosticsResult> history = manager.getResultHistory();
        for (ParserDiagnosticsResult result : history) {
            model.addElement(result);
        }

        resultsList.setModel(model);
    }

    @Nullable
    public ParserDiagnosticsResult getSelectedResult() {
        return resultsList.getSelectedValue();
    }

    public void selectResult(@Nullable ParserDiagnosticsResult result) {
        if (result != null) {
            resultsList.setSelectedValue(result, true);
        }
    }

    private static class ResultListCellRenderer extends ColoredListCellRenderer<ParserDiagnosticsResult> {
        @Override
        protected void customizeCellRenderer(@NotNull JList list, ParserDiagnosticsResult value, int index, boolean selected, boolean hasFocus) {
            append(value.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
    }

    @Nullable
    @Override
    public Object getData(@NotNull String dataId) {
        if (DataKeys.PARSER_DIAGNOSTICS_FORM.is(dataId)) {
            return this;
        }
        return super.getData(dataId);
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }
}