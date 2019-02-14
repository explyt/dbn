package com.dci.intellij.dbn.execution.statement;

import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.thread.ReadActionRunner;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.execution.ExecutionContext;
import com.dci.intellij.dbn.execution.ExecutionOption;
import com.dci.intellij.dbn.execution.ExecutionTarget;
import com.dci.intellij.dbn.execution.LocalExecutionInput;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariablesBundle;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.ExecutableBundlePsiElement;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StatementExecutionInput extends LocalExecutionInput {
    private StatementExecutionProcessor executionProcessor;
    private StatementExecutionVariablesBundle executionVariables;

    private String originalStatementText;
    private String executableStatementText;
    private ExecutablePsiElement executablePsiElement;
    private boolean bulkExecution = false;

    public StatementExecutionInput(String originalStatementText, String executableStatementText, StatementExecutionProcessor executionProcessor) {
        super(executionProcessor.getProject(), ExecutionTarget.STATEMENT);
        this.executionProcessor = executionProcessor;
        ConnectionHandler connectionHandler = executionProcessor.getConnectionHandler();
        DBSchema currentSchema = executionProcessor.getTargetSchema();
        DatabaseSession targetSession = executionProcessor.getTargetSession();

        this.targetConnectionRef = ConnectionHandlerRef.from(connectionHandler);
        this.targetSchemaRef = DBObjectRef.from(currentSchema);
        this.setTargetSession(targetSession);
        this.originalStatementText = originalStatementText;
        this.executableStatementText = executableStatementText;

        if (DatabaseFeature.DATABASE_LOGGING.isSupported(connectionHandler)) {
            connectionHandler = Failsafe.get(connectionHandler);
            getOptions().set(ExecutionOption.ENABLE_LOGGING, connectionHandler.isLoggingEnabled());
        }
    }

    @Override
    protected ExecutionContext createExecutionContext() {
        return new ExecutionContext() {
            @NotNull
            @Override
            public String getTargetName() {
                ExecutablePsiElement executablePsiElement = getExecutablePsiElement();
                return CommonUtil.nvl(executablePsiElement == null ? null : executablePsiElement.getPresentableText(), "Statement");
            }

            @Nullable
            @Override
            public ConnectionHandler getTargetConnection() {
                return getConnectionHandler();
            }

            @Nullable
            @Override
            public DBSchema getTargetSchema() {
                return StatementExecutionInput.this.getTargetSchema();
            }
        };
    }

    public int getExecutableLineNumber() {
        return executionProcessor == null ? 0 : executionProcessor.getExecutableLineNumber();
    }

    public String getOriginalStatementText() {
        return originalStatementText;
    }

    public void setOriginalStatementText(String originalStatementText) {
        this.originalStatementText = originalStatementText;
        executablePsiElement = null;
    }

    public void setExecutableStatementText(String executableStatementText) {
        this.executableStatementText = executableStatementText;
    }

    public String getExecutableStatementText() {
        return executableStatementText;
    }

    @Nullable
    public ExecutablePsiElement getExecutablePsiElement() {
        if (executablePsiElement == null) {
            ConnectionHandler connectionHandler = getConnectionHandler();
            DBSchema currentSchema = getTargetSchema();
            if (connectionHandler != null) {
                executablePsiElement = ReadActionRunner.invoke(false, () -> {
                    DBLanguageDialect languageDialect = executionProcessor.getPsiFile().getLanguageDialect();
                    DBLanguagePsiFile previewFile = DBLanguagePsiFile.createFromText(
                            getProject(),
                            "preview",
                            languageDialect,
                            originalStatementText,
                            connectionHandler,
                            currentSchema);

                    PsiElement firstChild = previewFile.getFirstChild();
                    if (firstChild instanceof ExecutableBundlePsiElement) {
                        ExecutableBundlePsiElement rootPsiElement = (ExecutableBundlePsiElement) firstChild;
                        List<ExecutablePsiElement> executablePsiElements = rootPsiElement.getExecutablePsiElements();
                        return executablePsiElements.isEmpty() ? null : executablePsiElements.get(0);
                    }
                    return null;
                });
            }
        }
        return executablePsiElement;
    }

    public StatementExecutionVariablesBundle getExecutionVariables() {
        return executionVariables;
    }

    public void setExecutionVariables(StatementExecutionVariablesBundle executionVariables) {
        if (this.executionVariables != null) {
            DisposerUtil.dispose(this.executionVariables);
        }
        this.executionVariables = executionVariables;
    }

    public PsiFile createPreviewFile() {
        ConnectionHandler activeConnection = getConnectionHandler();
        DBSchema currentSchema = getTargetSchema();
        DBLanguageDialect languageDialect = activeConnection == null ?
                SQLLanguage.INSTANCE.getMainLanguageDialect() :
                activeConnection.getLanguageDialect(SQLLanguage.INSTANCE);

        return DBLanguagePsiFile.createFromText(getProject(), "preview", languageDialect, executableStatementText, activeConnection, currentSchema);
    }

    public StatementExecutionProcessor getExecutionProcessor() {
        return executionProcessor;
    }

    @Override
    @Nullable
    public ConnectionHandler getConnectionHandler() {
        return ConnectionHandlerRef.get(targetConnectionRef);
    }

    @Override
    public boolean hasExecutionVariables() {
        return true;
    }

    @Override
    public boolean isSchemaSelectionAllowed() {
        return false;
    }

    @Override
    public boolean isSessionSelectionAllowed() {
        return false;
    }

    public void setConnectionHandler(ConnectionHandler connectionHandler) {
        this.targetConnectionRef = ConnectionHandlerRef.from(connectionHandler);
        if (DatabaseFeature.DATABASE_LOGGING.isSupported(connectionHandler)) {
            getOptions().set(ExecutionOption.ENABLE_LOGGING, connectionHandler.isLoggingEnabled());
        }
    }

    public ConnectionId getConnectionId() {
        return targetConnectionRef == null ? null : targetConnectionRef.getConnectionId();
    }

    public boolean isBulkExecution() {
        return bulkExecution;
    }

    public void setBulkExecution(boolean isBulkExecution) {
        this.bulkExecution = isBulkExecution;
    }

    @Override
    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            executionProcessor = null;
            executablePsiElement = null;
        }
    }

    public String getStatementDescription() {
        ExecutablePsiElement executablePsiElement = getExecutablePsiElement();
        return executablePsiElement == null ? "SQL Statement" : executablePsiElement.getPresentableText();
    }

    @Override
    public boolean isDatabaseLogProducer() {
        return executablePsiElement != null && executablePsiElement.getElementType().is(ElementTypeAttribute.DATABASE_LOG_PRODUCER);
    }
}
