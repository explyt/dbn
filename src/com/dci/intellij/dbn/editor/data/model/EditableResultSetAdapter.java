package com.dci.intellij.dbn.editor.data.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.connection.transaction.ConnectionSavepointCall;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.value.ValueAdapter;

public class EditableResultSetAdapter extends ResultSetAdapter {
    private ResultSet resultSet;

    public EditableResultSetAdapter(DatasetEditorModel model, ResultSet resultSet) {
        super(model);
        this.resultSet = resultSet;
    }

    @Override
    public void scroll(final int rowIndex) throws SQLException {
        if (!isInsertMode()) {
            if (isUseSavePoints()) {
                ConnectionSavepointCall.invoke(resultSet, () -> absolute(resultSet, rowIndex));
            } else {
                absolute(resultSet, rowIndex);
            }
        }
    }

    @Override
    public void updateRow() throws SQLException {
        if (!isInsertMode())  {
            if (isUseSavePoints()) {
                ConnectionSavepointCall.invoke(resultSet, () -> updateRow(resultSet));
            } else {
                updateRow(resultSet);
            }
        }
    }

    @Override
    public void refreshRow() throws SQLException {
        if (!isInsertMode())  {
            if (isUseSavePoints()) {
                ConnectionSavepointCall.invoke(resultSet, () -> refreshRow(resultSet));
            } else {
                refreshRow(resultSet);
            }
        }
    }

    @Override
    public void startInsertRow() throws SQLException {
        if (!isInsertMode())  {
            if (isUseSavePoints()) {
                ConnectionSavepointCall.invoke(resultSet, () -> {
                    moveToInsertRow(resultSet);
                    setInsertMode(true);
                });
            } else {
                moveToInsertRow(resultSet);
                setInsertMode(true);
            }
        }
    }

    @Override
    public void cancelInsertRow() throws SQLException {
        if (isInsertMode())  {
            if (isUseSavePoints()) {
                ConnectionSavepointCall.invoke(resultSet, () -> {
                    moveToCurrentRow(resultSet);
                    setInsertMode(false);
                });
            } else {
                moveToCurrentRow(resultSet);
                setInsertMode(false);
            }
        }
    }

    @Override
    public void insertRow() throws SQLException {
        if (isInsertMode())  {
            if (isUseSavePoints()) {
                ConnectionSavepointCall.invoke(resultSet, () -> {
                    insertRow(resultSet);
                    moveToCurrentRow(resultSet);
                    setInsertMode(false);
                });
            } else {
                insertRow(resultSet);
                moveToCurrentRow(resultSet);
                setInsertMode(false);
            }
        }
    }

    @Override
    public void deleteRow() throws SQLException {
        if (!isInsertMode())  {
            if (isUseSavePoints()) {
                ConnectionSavepointCall.invoke(resultSet, () -> deleteRow(resultSet));
            } else {
                deleteRow(resultSet);
            }
        }
    }

    @Override
    public void setValue(final int columnIndex, @NotNull final ValueAdapter valueAdapter, @Nullable final Object value) throws SQLException {
        Connection connection = resultSet.getStatement().getConnection();
        if (isUseSavePoints()) {
            ConnectionSavepointCall.invoke(resultSet,
                    () -> valueAdapter.write(connection, resultSet, columnIndex, value));
        } else {
            valueAdapter.write(connection, resultSet, columnIndex, value);
        }
    }

    @Override
    public void setValue(final int columnIndex, @NotNull final DBDataType dataType, @Nullable final Object value) throws SQLException {
        if (isUseSavePoints()) {
            ConnectionSavepointCall.invoke(resultSet,
                    () -> dataType.setValueToResultSet(resultSet, columnIndex, value));
        } else {
            dataType.setValueToResultSet(resultSet, columnIndex, value);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        resultSet = null;
    }
}
