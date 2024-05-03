package com.dbn.data.find;

import com.dbn.common.color.Colors;
import com.dbn.common.compatibility.CompatibilityUtil;
import com.dbn.common.ui.form.DBNFormBase;
import com.dbn.common.ui.util.*;
import com.dbn.common.util.Actions;
import com.dbn.common.util.Strings;
import com.dbn.data.find.action.*;
import com.dbn.data.grid.ui.table.basic.BasicTable;
import com.dbn.data.model.DataModel;
import com.dbn.data.model.DataModelListener;
import com.intellij.featureStatistics.FeatureUsageTracker;
import com.intellij.find.FindInProjectSettings;
import com.intellij.find.FindManager;
import com.intellij.find.FindModel;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.LightColors;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.dbn.common.ui.util.TextFields.onTextChange;
import static com.dbn.diagnostics.Diagnostics.conditionallyLog;

public class DataSearchComponent extends DBNFormBase implements SelectionListener, DataSearchResultListener, DataModelListener {
    private static final int MATCHES_LIMIT = 10000;

    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JTextField searchField;
    private JLabel matchesLabel;
    private JLabel closeLabel;

    private DataFindModel findModel;
    private boolean myListeningSelection = false;
    private ActionToolbar actionsToolbar;
    private final DataSearchResultController searchResultController;

    public JTextField getSearchField() {
        return searchField;
    }

    public DataSearchComponent(@NotNull SearchableDataComponent searchableComponent) {
        super(searchableComponent);
        BasicTable<?> table = searchableComponent.getTable();
        DataModel<?, ?> dataModel = table.getModel();
        dataModel.addDataModelListener(this);
        initializeFindModel();

        findModel = new DataFindModel();
        DataSearchResult searchResult = dataModel.getSearchResult();
        searchResult.setMatchesLimit(MATCHES_LIMIT);
        searchResult.addListener(this);
        searchResultController = new DataSearchResultController(searchableComponent);
        searchResultController.updateResult(findModel);

        configureLeadPanel();

        findModel.addObserver(findModel -> {
            String stringToFind = findModel.getStringToFind();
            if (!wholeWordsApplicable(stringToFind)) {
                findModel.setWholeWordsOnly(false);
            }
            updateUIWithFindModel();
            updateResults(true);
            FindManager findManager = getFindManager();
            syncFindModels(findManager.getFindInFileModel(), DataSearchComponent.this.findModel);
        });

        closeLabel.setText(" ");
        closeLabel.setIcon(AllIcons.Actions.Close);
        closeLabel.addMouseListener(Mouse.listener().onClick(e -> close()));

        updateUIWithFindModel();
        //new CloseOnESCAction(this, table);
        new PrevOccurrenceAction(this, table, false);
        new NextOccurrenceAction(this, table, false);
        searchableComponent.getTable().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!e.isConsumed()) {
                    if (e.getKeyChar() == Keyboard.Key.ESCAPE) {
                        searchableComponent.hideSearchHeader();
                    }
                }
            }
        });
    }

    @NotNull
    public SearchableDataComponent getSearchableComponent() {
        return this.ensureParentComponent();
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return searchField;
    }

    @Override
    public void modelChanged() {
        searchResultController.updateResult(findModel);
    }

    @Override
    public void searchResultUpdated(DataSearchResult searchResult) {
        int count = searchResult.size();
        if (searchField.getText().isEmpty()) {
            updateUIWithEmptyResults();
        } else {
            if (count <= searchResult.getMatchesLimit()) {
                if (count > 0) {
                    setRegularBackground();
                    if (count > 1) {
                        matchesLabel.setText(count + " matches");
                    } else {
                        matchesLabel.setText("1 match");
                    }
                } else {
                    setNotFoundBackground();
                    matchesLabel.setText("No matches");
                }
            } else {
                setRegularBackground();
                matchesLabel.setText("More than " + searchResult.getMatchesLimit() + " matches");
                boldMatchInfo();
            }
        }
    }

    public void initializeFindModel() {
        if (findModel == null) {
            findModel = new DataFindModel();
            FindManager findManager = getFindManager();
            findModel.copyFrom(findManager.getFindInFileModel());
            findModel.setPromptOnReplace(false);
        }
/*
        String stringToFind = searchableComponent.getSelectedText();
        findModel.setStringToFind(StringUtil.isEmpty(stringToFind) ? "" : stringToFind);
*/
    }
    
    public void resetFindModel() {
        if (findModel != null) {
            findModel.setStringToFind("");
        }
    }

    private void configureLeadPanel() {
        initTextField();
        onTextChange(searchField, e -> searchFieldDocumentChanged());

        DefaultActionGroup myActionsGroup = new DefaultActionGroup("Search Bar", false);
        myActionsGroup.add(new ShowHistoryAction(searchField, this));
        myActionsGroup.add(new PrevOccurrenceAction(this, searchField, true));
        myActionsGroup.add(new NextOccurrenceAction(this, searchField, true));
        //myActionsGroup.add(new FindAllAction(this));
        myActionsGroup.add(new ToggleMatchCase(this));
        myActionsGroup.add(new ToggleRegex(this));

        actionsToolbar = Actions.createActionToolbar(actionsPanel, "SearchBar", true, myActionsGroup);

        myActionsGroup.addAction(new ToggleWholeWordsOnlyAction(this));

        actionsToolbar.setLayoutPolicy(ActionToolbar.AUTO_LAYOUT_POLICY);
        actionsPanel.add(actionsToolbar.getComponent(), BorderLayout.CENTER);

        setSmallerFontAndOpaque(matchesLabel);


        JLabel closeLabel = new JLabel(" ", AllIcons.Actions.Close, SwingConstants.RIGHT);
        closeLabel.addMouseListener(Mouse.listener().onClick(e -> close()));

        closeLabel.setToolTipText("Close search bar (Escape)");
        CompatibilityUtil.setSmallerFont(searchField);

        searchField.registerKeyboardAction(e -> {
            if (Strings.isEmptyOrSpaces(searchField.getText())) {
                close();
            } else {
                // TODO
                //requestFocus(myEditor.getContentComponent());
                addTextToRecent(DataSearchComponent.this.searchField);
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, UserInterface.ctrlDownMask()),
                JComponent.WHEN_FOCUSED);

        final String initialText = findModel.getStringToFind();

        ApplicationManager.getApplication().invokeLater(() -> setInitialText(initialText));
    }

    private void searchFieldDocumentChanged() {
        String text = searchField.getText();
        findModel.setStringToFind(text);
        if (!Strings.isEmpty(text)) {
            updateResults(true);
        } else {
            nothingToSearchFor();
        }
    }

    public boolean isRegexp() {
        return findModel.isRegularExpressions();
    }

    public void setRegexp(boolean val) {
        findModel.setRegularExpressions(val);
    }

    public FindModel getFindModel() {
        return findModel;
    }

    private static void syncFindModels(FindModel to, FindModel from) {
        to.setCaseSensitive(from.isCaseSensitive());
        to.setWholeWordsOnly(from.isWholeWordsOnly());
        to.setRegularExpressions(from.isRegularExpressions());
        to.setSearchContext(from.getSearchContext());
    }

    private void updateUIWithFindModel() {
        String stringToFind = findModel.getStringToFind();

        if (!Objects.equals(stringToFind, searchField.getText())) {
            searchField.setText(stringToFind);
        }

        setTrackingSelection(!findModel.isGlobal());
    }

    private static boolean wholeWordsApplicable(String stringToFind) {
        return !stringToFind.startsWith(" ") &&
                !stringToFind.startsWith("\t") &&
                !stringToFind.endsWith(" ") &&
                !stringToFind.endsWith("\t");
    }

    private void setTrackingSelection(boolean b) {
        if (b) {
            if (!myListeningSelection) {
                // TODO
                //myEditor.getSelectionModel().addSelectionListener(this);
            }
        } else {
            if (myListeningSelection) {
                // TODO
                //myEditor.getSelectionModel().removeSelectionListener(this);
            }
        }
        myListeningSelection = b;
    }

    public void showHistory(boolean byClickingToolbarButton, JTextField textField) {
        FeatureUsageTracker.getInstance().triggerFeatureUsed("find.recent.search");
        FindInProjectSettings settings = getFindSettings();
        String[] recent = textField == searchField ? settings.getRecentFindStrings() : settings.getRecentReplaceStrings();
        List<String> recentOptions = Arrays.asList(ArrayUtil.reverseArray(recent));
        Popups.showCompletionPopup(byClickingToolbarButton ? actionsPanel : null, recentOptions, "Recent Searches", textField, null);
    }

    private void initTextField() {
        //searchField.setColumns(25);
        if (CompatibilityUtil.isUnderGTKLookAndFeel()) {
            searchField.setOpaque(false);
        }
        searchField.putClientProperty("AuxEditorComponent", Boolean.TRUE);
        searchField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent e) {
                UserInterface.repaint(searchField);
            }

            @Override
            public void focusLost(final FocusEvent e) {
                UserInterface.repaint(searchField);
            }
        });
        new CloseOnESCAction(this, searchField);
    }


    public void setInitialText(final String initialText) {
        String text = initialText != null ? initialText : "";
        setTextInField(text);
        searchField.selectAll();
    }

    private void requestFocus(Component component) {
        Project project = getSearchableComponent().getTable().getProject();
        IdeFocusManager.getInstance(project).requestFocus(component, true);
    }

    public void searchBackward() {
        moveCursor(DataSearchDirection.UP);
        addTextToRecent(searchField);
    }

    public void searchForward() {
        moveCursor(DataSearchDirection.DOWN);
        addTextToRecent(searchField);
    }

    private void addTextToRecent(JTextComponent textField) {
        final String text = textField.getText();
        if (text.length() > 0) {
            if (textField == searchField) {
                getFindSettings().addStringToFind(text);
            } else {
                getFindSettings().addStringToReplace(text);
            }
        }
    }

    private FindInProjectSettings getFindSettings() {
        return FindInProjectSettings.getInstance(ensureProject());
    }

    @Override
    public void selectionChanged(@NotNull SelectionEvent e) {
        updateResults(true);
    }

    private void moveCursor(DataSearchDirection direction) {
        searchResultController.moveCursor(direction);
    }

    private static void setSmallerFontAndOpaque(final JComponent component) {
        CompatibilityUtil.setSmallerFont(component);
        component.setOpaque(false);
    }

    public void requestFocus() {
        searchField.setSelectionStart(0);
        searchField.setSelectionEnd(searchField.getText().length());
        requestFocus(searchField);
    }

    public void close() {
        getSearchResult().clear();
        getSearchableComponent().hideSearchHeader();
    }

    public void removeNotify() {
/*
        // TODO
        myLivePreview.cleanUp();
        myLivePreview.dispose();
*/
        setTrackingSelection(false);
        addTextToRecent(searchField);
    }

    private void updateResults(final boolean allowedToChangedEditorSelection) {
        matchesLabel.setFont(Fonts.deriveFont(matchesLabel.getFont(), Font.PLAIN));
        String text = searchField.getText();
        if (text.length() == 0) {
            nothingToSearchFor();
            SearchableDataComponent searchableComponent = getSearchableComponent();
            searchableComponent.cancelEditActions();
            BasicTable table = getSearchableComponent().getTable();
            table.clearSelection();
            UserInterface.repaint(table);
        } else {

            if (findModel.isRegularExpressions()) {
                try {
                    Pattern.compile(text);
                } catch (Exception e) {
                    conditionallyLog(e);
                    setNotFoundBackground();
                    matchesLabel.setText("Incorrect regular expression");
                    boldMatchInfo();
                    getSearchResult().clear();
                    return;
                }
            }

            FindManager findManager = getFindManager();
            if (allowedToChangedEditorSelection) {
                findManager.setFindWasPerformed();
                FindModel copy = new FindModel();
                copy.copyFrom(findModel);
                copy.setReplaceState(false);
                findManager.setFindNextModel(copy);
            }

            searchResultController.updateResult(findModel);
        }
    }

    private FindManager getFindManager() {
        Project project = getSearchableComponent().getTable().getProject();
        return FindManager.getInstance(project);
    }

    private void nothingToSearchFor() {
        updateUIWithEmptyResults();
        getSearchResult().clear();
    }

    private void updateUIWithEmptyResults() {
        setRegularBackground();
        matchesLabel.setText("");
    }

    private void boldMatchInfo() {
        matchesLabel.setFont(Fonts.deriveFont(matchesLabel.getFont(), Font.BOLD));
    }


    private void setNotFoundBackground() {
        searchField.setBackground(LightColors.RED);
    }

    private void setRegularBackground() {
        searchField.setBackground(Colors.getTextFieldBackground());
    }

    public String getTextInField() {
        return searchField.getText();
    }

    public void setTextInField(final String text) {
        searchField.setText(text);
        findModel.setStringToFind(text);
    }

    public boolean hasMatches() {
        DataSearchResult searchResult = getSearchResult();
        return searchResult != null && !searchResult.isEmpty();
    }

    private DataSearchResult getSearchResult() {
        return getSearchableComponent().getTable().getModel().getSearchResult();
    }

}
