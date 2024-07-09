package com.dbn.editor.data.filter.ui;

import com.dbn.common.dispose.DisposableContainers;
import com.dbn.common.dispose.Disposer;
import com.dbn.common.dispose.Failsafe;
import com.dbn.common.options.ui.ConfigurationEditorForm;
import com.dbn.common.ui.CardLayouts;
import com.dbn.common.ui.form.DBNHeaderForm;
import com.dbn.common.ui.util.Fonts;
import com.dbn.common.util.Actions;
import com.dbn.editor.data.filter.DatasetFilter;
import com.dbn.editor.data.filter.DatasetFilterGroup;
import com.dbn.editor.data.filter.DatasetFilterImpl;
import com.dbn.editor.data.filter.DatasetFilterManager;
import com.dbn.editor.data.filter.action.CreateFilterAction;
import com.dbn.editor.data.filter.action.DeleteFilterAction;
import com.dbn.editor.data.filter.action.MoveFilterDownAction;
import com.dbn.editor.data.filter.action.MoveFilterUpAction;
import com.dbn.object.DBDataset;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class DatasetFilterForm extends ConfigurationEditorForm<DatasetFilterGroup> implements ListSelectionListener {
    private final Map<String, ConfigurationEditorForm> filterDetailPanels = DisposableContainers.map(this);

    private JPanel mainPanel;
    private JList filtersList;
    private JPanel filterDetailsPanel;
    private JPanel actionsPanel;
    private JPanel headerPanel;

    public DatasetFilterForm(DatasetFilterGroup filterGroup, @NotNull DBDataset dataset) {
        super(filterGroup);
        filtersList.setModel(filterGroup);
        filtersList.setFont(Fonts.getLabelFont());
        Project project = dataset.getProject();

        DBNHeaderForm headerForm = new DBNHeaderForm(this, dataset);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

        DatasetFilterList filters = getFilterList();
        ActionToolbar actionToolbar = Actions.createActionToolbar(
                actionsPanel,
                "DBNavigator.DataEditor.FiltersList", true,
                new CreateFilterAction(filters),
                new DeleteFilterAction(filters),
                new MoveFilterUpAction(filters),
                new MoveFilterDownAction(filters));
        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.CENTER);
        CardLayouts.addBlankCard(filterDetailsPanel);

        DatasetFilterManager filterManager = DatasetFilterManager.getInstance(project);
        DatasetFilter filter = filterManager.getActiveFilter(dataset);
        if (filter != null) {
            filtersList.setSelectedValue(filter, true);
        }
        valueChanged(null);
        filtersList.addListSelectionListener(this);
    }

    public DatasetFilterList getFilterList() {
        return (DatasetFilterList) filtersList;
    }

    public DatasetFilter getSelectedFilter() {
        return (DatasetFilter) filtersList.getSelectedValue();
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        getFilterList().getFilterGroup().apply();
    }

    @Override
    public void resetFormChanges() {
        getFilterList().getFilterGroup().reset();
    }

    private void createUIComponents() {
        filtersList = new DatasetFilterList();
    }


    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e != null && e.getValueIsAdjusting()) return;
        Failsafe.guarded(this, f -> f.updateFilters());
    }

    private void updateFilters() {
        DatasetFilterGroup configuration = getConfiguration();
        List<DatasetFilter> filters = configuration.getFilters();
        DatasetFilterImpl filter = null;
        int filtersCount = filters.size();

        int[] indices = filtersList.getSelectedIndices();
        if (filtersCount > 0 && indices.length == 1) {
            if (filtersCount > indices[0]) {
                filter = (DatasetFilterImpl) filters.get(indices[0]);
            }
        }

        if (filter == null) {
            CardLayouts.showBlankCard(filterDetailsPanel);
        } else {
            String id = filter.getId();
            ConfigurationEditorForm configurationEditorForm = filterDetailPanels.get(id);
            if (configurationEditorForm == null) {
                JComponent component = filter.createComponent();
                CardLayouts.addCard(filterDetailsPanel, component, id);

                configurationEditorForm = filter.ensureSettingsEditor();
                filterDetailPanels.put(id, configurationEditorForm);

                Disposer.register(this, configurationEditorForm);
            }
            CardLayouts.showCard(filterDetailsPanel, id);
            configurationEditorForm.focus();
        }
    }
}
