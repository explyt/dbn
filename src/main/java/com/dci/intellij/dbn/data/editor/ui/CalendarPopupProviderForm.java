package com.dci.intellij.dbn.data.editor.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.color.Colors;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.common.ui.util.*;
import com.dci.intellij.dbn.common.util.Actions;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;

public class CalendarPopupProviderForm extends TextFieldPopupProviderForm implements TableModelListener {
    private static final TableCellRenderer CELL_RENDERER = new CalendarTableCellRenderer();
    private static final TableCellRenderer HEADER_CELL_RENDERER = new CalendarTableHeaderCellRenderer();
    private final TableModel CALENDER_HEADER_TABLE_MODEL = new CalendarHeaderTableModel();
    private JPanel mainPanel;
    private JTable daysTable;
    private JTable weeksTable;
    private JLabel monthYearLabel;
    private JPanel calendarPanel;
    private JPanel actionsLeftPanel;
    private JPanel actionsRightPanel;
    private JPanel timePanel;
    private JTextField timeTextField;
    private JLabel timeLabel;
    private JPanel actionsPanelBottom;
    private JPanel headerSeparatorPanel;

    CalendarPopupProviderForm(TextFieldWithPopup<?> textField, boolean autoPopup) {
        super(textField, autoPopup, true);
        calendarPanel.setBackground(weeksTable.getBackground());
        daysTable.addKeyListener(this);
        timeTextField.addKeyListener(this);

        weeksTable.setDefaultRenderer(Object.class, HEADER_CELL_RENDERER);
        weeksTable.setFocusable(false);
        weeksTable.setShowGrid(false);
        calendarPanel.setBorder(Borders.COMPONENT_LINE_BORDER);
        headerSeparatorPanel.setBorder(Borders.BOTTOM_LINE_BORDER);

        daysTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        daysTable.setDefaultRenderer(Object.class, CELL_RENDERER);
        daysTable.getTableHeader().setDefaultRenderer(HEADER_CELL_RENDERER);
        daysTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        daysTable.setShowGrid(false);
        daysTable.addMouseListener(Mouse.listener().onClick(e -> {
            if (e.getButton() == MouseEvent.BUTTON1) {
                selectDate();
            }
        }));

        /*tDays.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                final Point point = e.getPoint();
                int rowIndex = tDays.rowAtPoint(point);
                int columnIndex = tDays.columnAtPoint(point);
                tDays.setRowSelectionInterval(rowIndex, rowIndex);
                tDays.setColumnSelectionInterval(columnIndex, columnIndex);
            }
        });*/

        ActionToolbar actionToolbarLeft = Actions.createActionToolbar(
                getMainComponent(),
                "DBNavigator.Place.DataEditor.CalendarPopup", true,
                new PreviousYearAction(),
                new PreviousMonthAction());
        ActionToolbar actionToolbarRight = Actions.createActionToolbar(
                getMainComponent(),
                "DBNavigator.Place.DataEditor.CalendarPopup", true,
                new NextMonthAction(),
                new NextYearAction());

        ActionToolbar actionToolbarBottom = Actions.createActionToolbar(
                getMainComponent(),
                "DBNavigator.Place.DataEditor.CalendarPopup",
                true,
                new ClearTimeAction());

        actionsLeftPanel.add(actionToolbarLeft.getComponent(), BorderLayout.WEST);
        actionsRightPanel.add(actionToolbarRight.getComponent(), BorderLayout.EAST);
        actionsPanelBottom.add(actionToolbarBottom.getComponent(), BorderLayout.EAST);

        updateComponentColors();
        Colors.subscribe(this, () -> updateComponentColors());
    }

    private void updateComponentColors() {
        Failsafe.nd(this);
        Color panelBackground = Colors.getPanelBackground();
        Color labelForeground = Colors.getLabelForeground();
        Color tableBackground = Colors.getTableBackground();

        UserInterface.changePanelBackground(mainPanel, panelBackground);
        timeLabel.setForeground(labelForeground);
        monthYearLabel.setForeground(labelForeground);
        daysTable.setBackground(tableBackground);
        weeksTable.setBackground(tableBackground);
        calendarPanel.setBackground(tableBackground);
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    public Formatter getFormatter() {
        return Formatter.getInstance(ensureProject());
    }

    private Date getDateForPopup() {
        if (getEditorComponent().getUserValueHolder() == null) {
            String dateString = getEditorComponent().getTextField().getText();
            try {
                return getFormatter().parseDateTime(dateString);
            } catch (ParseException e) {
                return new Date();
            }
        } else {
            Object userValue = getEditorComponent().getUserValueHolder().getUserValue();
            return userValue instanceof Date ? (Date) userValue : new Date();
        }
    }

    @Override
    public JBPopup createPopup() {
        Date date = getDateForPopup();
        CalendarTableModel tableModel = new CalendarTableModel(date);
        tableModel.addTableModelListener(this);

        daysTable.setModel(tableModel);
        weeksTable.setModel(CALENDER_HEADER_TABLE_MODEL);

        int rowIndex = tableModel.getInputDateRowIndex();
        int columnIndex = tableModel.getInputDateColumnIndex();
        daysTable.setRowSelectionInterval(rowIndex, rowIndex);
        daysTable.setColumnSelectionInterval(columnIndex, columnIndex);

        ComponentPopupBuilder popupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(mainPanel, daysTable);
        popupBuilder.setRequestFocus(true);

        monthYearLabel.setText(tableModel.getCurrentMonthName() + " " + tableModel.getCurrentYear());

        timeTextField.setText(getFormatter().formatTime(date));
        timeLabel.setText("Time (" + getFormatter().getTimeFormatPattern() + ")");

        return popupBuilder.createPopup();
    }

    @Override
    public void handleKeyPressedEvent(KeyEvent e) {}

    @Override
    public void handleKeyReleasedEvent(KeyEvent e) {}

    @Override
    public void handleFocusLostEvent(FocusEvent e) {}


    @Override
    public String getKeyShortcutName() {
        return IdeActions.ACTION_SHOW_INTENTION_ACTIONS;
    }

    @Override
    public String getDescription() {
        return "Calendar";
    }

    @Override
    public TextFieldPopupType getPopupType() {
        return TextFieldPopupType.CALENDAR;
    }

    @Nullable
    @Override
    public Icon getButtonIcon() {
        return Icons.DATA_EDITOR_CALENDAR;
    }

    private void selectDate() {
        CalendarTableModel model = (CalendarTableModel) daysTable.getModel();
        Date date = model.getTimestamp(daysTable.getSelectedRow(), daysTable.getSelectedColumn());
        TextFieldWithPopup editorComponent = getEditorComponent();
        editorComponent.setText(getFormatter().formatDateTime(date));
        hidePopup();
        getTextField().requestFocus();
    }

    /******************************************************
     *                   KeyListener                      *
     ******************************************************/
    @Override
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);
        if (!e.isConsumed()) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER ) {
                selectDate();
            }
            if (e.getKeyCode() == KeyEvent.VK_TAB  && e.getSource() == daysTable) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent(daysTable);
                e.consume();
            }
        }
    }

    /******************************************************
     *                TableModelListener                  *
     ******************************************************/
    @Override
    public void tableChanged(TableModelEvent e) {
        CalendarTableModel model = (CalendarTableModel) daysTable.getModel();
        monthYearLabel.setText(model.getCurrentMonthName() + " " + model.getCurrentYear());
    }

    /******************************************************
     *                  TableModels                       *
     ******************************************************/
    private static class CalendarHeaderTableModel implements TableModel {
        @Override
        public int getRowCount() {
            return 1;
        }

        @Override
        public int getColumnCount() {
            return 7;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return null;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0: return "S";
                case 1: return "M";
                case 2: return "T";
                case 3: return "W";
                case 4: return "T";
                case 5: return "F";
                case 6: return "S";
            }
            return null;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {}
        @Override
        public void addTableModelListener(TableModelListener l) {}
        @Override
        public void removeTableModelListener(TableModelListener l) {}
    }

    private class CalendarTableModel implements TableModel {
        private final Set<TableModelListener> listeners = new HashSet<>();
        private final Calendar inputDate = new GregorianCalendar();
        private final Calendar activeMonth = new GregorianCalendar();
        private final Calendar previousMonth = new GregorianCalendar();

        private CalendarTableModel(Date date) {
            if (date != null) {
                inputDate.setTime(date);
                activeMonth.setTime(date);
                previousMonth.setTime(date);
            }
            activeMonth.set(Calendar.DAY_OF_MONTH, 1);
            previousMonth.set(Calendar.DAY_OF_MONTH, 1);
            previousMonth.add(Calendar.MONTH, -1);
        }

        private CalendarTableModel() {
            this(null);
        }

        @Override
        public int getRowCount() {
            return 6;
        }

        @Override
        public int getColumnCount() {
            return 7;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return null;
        }

        String getCurrentMonthName() {
            return getMonthName(activeMonth.get(Calendar.MONTH));
        }

        String getCurrentYear() {
            return String.valueOf(activeMonth.get(Calendar.YEAR));
        }

        String getMonthName(int month) {
            switch (month) {
                case Calendar.JANUARY: return "January";
                case Calendar.FEBRUARY: return "February";
                case Calendar.MARCH: return "March";
                case Calendar.APRIL: return "April";
                case Calendar.MAY: return "May";
                case Calendar.JUNE: return "June";
                case Calendar.JULY: return "July";
                case Calendar.AUGUST: return "August";
                case Calendar.SEPTEMBER: return "September";
                case Calendar.OCTOBER: return "October";
                case Calendar.NOVEMBER: return "November";
                case Calendar.DECEMBER: return "December";
            }
            return null;
        }

        private boolean isFromActiveMonth(int rowIndex, int columnIndex) {
            return !isFromPreviousMonth(rowIndex, columnIndex) &&
                   !isFromNextMonth(rowIndex, columnIndex);
        }

        private boolean isFromPreviousMonth(int rowIndex, int columnIndex) {
            int value = rowIndex * 7 + columnIndex + 2 - activeMonth.get(Calendar.DAY_OF_WEEK);
            return value < 1;
        }

        private boolean isFromNextMonth(int rowIndex, int columnIndex) {
            int value = rowIndex * 7 + columnIndex + 2 - activeMonth.get(Calendar.DAY_OF_WEEK);
            return value > activeMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
        }

        private boolean isInputDate(int rowIndex, int columnIndex) {
            int value = rowIndex * 7 + columnIndex + 2 - activeMonth.get(Calendar.DAY_OF_WEEK);
            return  inputDate.get(Calendar.YEAR) == activeMonth.get(Calendar.YEAR) &&
                    inputDate.get(Calendar.MONTH) == activeMonth.get(Calendar.MONTH) &&
                    value >= 1 && value <= activeMonth.getActualMaximum(Calendar.DAY_OF_MONTH) &&
                    value == inputDate.get(Calendar.DAY_OF_MONTH);
        }

        private int getInputDateColumnIndex() {
            return inputDate.get(Calendar.DAY_OF_WEEK) - 1;
        }

        private int getInputDateRowIndex() {
            return (activeMonth.get(Calendar.DAY_OF_WEEK) + inputDate.get(Calendar.DAY_OF_MONTH) - 2) / 7;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            int value = rowIndex * 7 + columnIndex + 2 - activeMonth.get(Calendar.DAY_OF_WEEK);
            if (value < 1) {
                value = previousMonth.getActualMaximum(Calendar.DAY_OF_MONTH) + value;
            } else if (value > activeMonth.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                value = value - activeMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
            }
            return value;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {}
        @Override
        public void addTableModelListener(TableModelListener l) {listeners.add(l);}
        @Override
        public void removeTableModelListener(TableModelListener l) {listeners.remove(l);}

        private void rollMonth(int amount) {
            activeMonth.add(Calendar.MONTH, amount);
            previousMonth.add(Calendar.MONTH, amount);
            notifyListeners();
        }

        private void rollYear(int amount) {
            activeMonth.add(Calendar.YEAR, amount);
            previousMonth.add(Calendar.YEAR, amount);
            notifyListeners();
        }

        private void notifyListeners() {
            TableModelEvent event = new TableModelEvent(this, 0, 5);
            for (TableModelListener listener : listeners) {
                listener.tableChanged(event);
            }
        }

        Timestamp getTimestamp(int rowIndex, int columnIndex) {
            Calendar calendar = (Calendar) activeMonth.clone();
            if (isFromPreviousMonth(rowIndex,  columnIndex)) {
                calendar.add(Calendar.MONTH, -1);
            } else if (isFromNextMonth(rowIndex,  columnIndex)) {
                calendar.add(Calendar.MONTH, 1);
            }
            int day = (Integer) getValueAt(rowIndex, columnIndex);
            calendar.set(Calendar.DAY_OF_MONTH, day);

            String timeText = timeTextField.getText();
            try {
                Date time = getFormatter().parseTime(timeText);
                Calendar timeCalendar = new GregorianCalendar();
                timeCalendar.setTime(time);
                calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
                calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
                calendar.set(Calendar.SECOND, timeCalendar.get(Calendar.SECOND));
                calendar.set(Calendar.MILLISECOND, timeCalendar.get(Calendar.MILLISECOND));
            } catch (ParseException e) {
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
            }

            return new Timestamp(calendar.getTime().getTime());
        }
    }

    /******************************************************
     *                       Actions                      *
     ******************************************************/
    private class NextMonthAction extends AnAction {
        private NextMonthAction() {
            super("Next Month", null, Icons.CALENDAR_CELL_EDIT_NEXT_MONTH);
            setShortcutSet(Keyboard.createShortcutSet(KeyEvent.VK_RIGHT, UserInterface.ctrlDownMask()));
            registerAction(this);
        }
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            CalendarTableModel tableModel = (CalendarTableModel) daysTable.getModel();
            tableModel.rollMonth(1);
        }
    }

    private class NextYearAction extends AnAction {
        private NextYearAction() {
            super("Next Year", null, Icons.CALENDAR_CELL_EDIT_NEXT_YEAR);
            setShortcutSet(Keyboard.createShortcutSet(KeyEvent.VK_UP, UserInterface.ctrlDownMask()));
            registerAction(this);
        }
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            CalendarTableModel tableModel = (CalendarTableModel) daysTable.getModel();
            tableModel.rollYear(1);
        }
    }

    private class PreviousMonthAction extends AnAction {
        private PreviousMonthAction() {
            super("Previous Month", null, Icons.CALENDAR_CELL_EDIT_PREVIOUS_MONTH);
            setShortcutSet(Keyboard.createShortcutSet(KeyEvent.VK_LEFT, UserInterface.ctrlDownMask()));
            registerAction(this);

        }
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            CalendarTableModel tableModel = (CalendarTableModel) daysTable.getModel();
            tableModel.rollMonth(-1);
        }
    }

    private class PreviousYearAction extends AnAction {
        private PreviousYearAction() {
            super("Previous Year", null, Icons.CALENDAR_CELL_EDIT_PREVIOUS_YEAR);
            setShortcutSet(Keyboard.createShortcutSet(KeyEvent.VK_DOWN, UserInterface.ctrlDownMask()));
            registerAction(this);
        }
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            CalendarTableModel tableModel = (CalendarTableModel) daysTable.getModel();
            tableModel.rollYear(-1);
        }
    }

    private class ClearTimeAction extends AnAction {
        private ClearTimeAction() {
            super("Reset Time", null, Icons.CALENDAR_CELL_EDIT_CLEAR_TIME);
            registerAction(this);
        }
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Calendar calendar = new GregorianCalendar(2000, Calendar.JANUARY,0,0,0,0);
            String timeString = getFormatter().formatTime(calendar.getTime());
            timeTextField.setText(timeString);
        }
    }

    /******************************************************
     *                  TableCellRenderers                *
     ******************************************************/
    private static class CalendarTableCellRenderer extends DefaultTableCellRenderer {
        static final Border SELECTION_BORDER = new CompoundBorder(new LineBorder(Colors.getLabelForeground(), 1, false), JBUI.Borders.emptyRight(6));
        static final Border EMPTY_BORDER = JBUI.Borders.empty(1, 1, 1, 9);
        static final Color INACTIVE_DAY_COLOR = new JBColor(new Color(0xC0C0C0), new Color(0x5B5B5B));

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            CalendarTableModel model = (CalendarTableModel) table.getModel();

            boolean isInputDate = model.isInputDate(row, column);
            boolean isFromActiveMonth = model.isFromActiveMonth(row, column);
            Color foreground =
                    isInputDate ? Colors.getTableForeground() :
                    isFromActiveMonth ? Colors.getLabelForeground() : INACTIVE_DAY_COLOR;

            setForeground(isSelected ? Colors.getTableSelectionForeground(true) : foreground);
            setHorizontalAlignment(RIGHT);
            setBorder(isInputDate && !isSelected ? SELECTION_BORDER : EMPTY_BORDER);
            setBackground(isSelected ? Colors.getListSelectionBackground(true) :  Colors.getTableBackground());
            //setBorder(new DottedBorder(Color.BLACK));
            return component;
        }
    }


    private static class CalendarTableHeaderCellRenderer extends DefaultTableCellRenderer {
        static final Border EMPTY_BORDER = JBUI.Borders.empty(1, 1, 1, 9);
        static final Color FOREGROUND_COLOR = new JBColor(new Color(67, 123, 203), new Color(67, 123, 203));

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(RIGHT);
            setFont(Fonts.BOLD);
            setBorder(EMPTY_BORDER);
            //setForeground(column == 0 ? Color.RED : GUIUtil.getTableForeground());
            setForeground(FOREGROUND_COLOR);
            return component;
        }
    }
}