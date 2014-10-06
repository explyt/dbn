package com.dci.intellij.dbn.execution.statement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ui.SelectConnectionDialog;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionBasicProcessor;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionCursorProcessor;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.language.common.psi.RootPsiElement;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

public class StatementExecutionManager extends AbstractProjectComponent {
    public static final String[] OPTIONS_MULTIPLE_STATEMENT_EXEC = new String[]{"Execute All", "Execute All from Caret", "Cancel"};
    private final Set<StatementExecutionProcessor> executionProcessors = new HashSet<StatementExecutionProcessor>();
    private static int sequence;
    public int getNextSequence() {
        sequence++;
        return sequence;
    }

    private StatementExecutionManager(Project project) {
        super(project);
    }

    public static StatementExecutionManager getInstance(Project project) {
        return project.getComponent(StatementExecutionManager.class);
    }

    public void fireExecution(final StatementExecutionProcessor executionProcessor) {
        boolean continueExecution = selectConnection(executionProcessor.getFile());
        new ConnectionAction(executionProcessor, continueExecution) {
            @Override
            protected void execute() {
                boolean continueExecution = executionProcessor.promptVariablesDialog();
                if (continueExecution) {
                    new BackgroundTask(getProject(), "Executing statement", false, true) {
                        public void execute(@NotNull ProgressIndicator progressIndicator) {
                            initProgressIndicator(progressIndicator, true);
                            executionProcessor.execute(progressIndicator);
                        }
                    }.start();
                }
            }
        }.start();
    }

    public void fireExecution(final List<StatementExecutionProcessor> executionProcessors) {
        if (executionProcessors.size() > 0) {
            DBLanguagePsiFile file =  executionProcessors.get(0).getFile();
            boolean continueExecution = selectConnection(file);
            if (continueExecution) {
                for (StatementExecutionProcessor executionProcessor : executionProcessors) {
                    continueExecution = executionProcessor.promptVariablesDialog();
                    if (!continueExecution) break;
                }
                if (continueExecution) {
                    new BackgroundTask(getProject(), "Executing statement", false, true) {
                        public void execute(@NotNull ProgressIndicator progressIndicator) {
                            boolean showIndeterminateProgress = executionProcessors.size() < 5;
                            initProgressIndicator(progressIndicator, showIndeterminateProgress);

                            for (int i = 0; i < executionProcessors.size(); i++) {
                                if (!progressIndicator.isCanceled()) {
                                    if (!progressIndicator.isIndeterminate()) {
                                        progressIndicator.setFraction(CommonUtil.getProgressPercentage(i, executionProcessors.size()));
                                    }

                                    executionProcessors.get(i).execute(progressIndicator);
                                }
                            }
                        }
                    }.start();
                }
            }
        }
    }

    private boolean selectConnection(DBLanguagePsiFile file) {
        ConnectionHandler activeConnection = file.getActiveConnection();
        DBSchema currentSchema = file.getCurrentSchema();
        if (activeConnection == null || currentSchema == null || activeConnection.isVirtual()) {
            String message =
                    activeConnection == null ?
                            "The file is not linked to any connection.\n" +
                            "To continue with the statement execution please select a target connection." :
                    activeConnection.isVirtual() ?
                            "The connection you selected for this file is a virtual connection, used only to decide the SQL dialect.\n" +
                            "You can not execute statements against this connection. Please select a proper connection to continue." :
                    currentSchema == null ?
                            "You did not select any schema to run the statement against.\n" +
                            "To continue with the statement execution please select a schema." : null;

            String okOption =
                    activeConnection == null || activeConnection.isVirtual() ? "Select Connection" :
                    currentSchema == null ? "Select Schema" : null;

            String[] options = {okOption, "Cancel"};

            int response = MessageUtil.showWarningDialog(message, "No valid Connection / Schema", options, 0);

            if (response == 0) {
                SelectConnectionDialog selectConnectionDialog = new SelectConnectionDialog(file);
                selectConnectionDialog.show();
                return selectConnectionDialog.getExitCode() == SelectConnectionDialog.OK_EXIT_CODE;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public void executeSelectedStatement(final Editor editor) {
        final DBLanguagePsiFile file = (DBLanguagePsiFile) DocumentUtil.getFile(editor);
        new ConnectionAction(file) {
            @Override
            protected void execute() {
                StatementExecutionProcessor executionProcessor = getExecutionProcessorAtCursor(editor);
                if (executionProcessor != null) {
                    fireExecution(executionProcessor);
                } else {
                    int exitCode = MessageUtil.showQuestionDialog(
                            "No statement found under the caret. \nExecute all statements in the file or just the ones after the cursor?",
                            "Multiple Statement Execution",
                            OPTIONS_MULTIPLE_STATEMENT_EXEC, 0);
                    if (exitCode == 0 || exitCode == 1) {
                        int offset = exitCode == 0 ? 0 : editor.getCaretModel().getOffset();
                        List<StatementExecutionProcessor> executionProcessors = getExecutionProcessors(file, offset);
                        fireExecution(executionProcessors);
                    }
                }
            }
        }.start();
    }

    @Nullable
    private StatementExecutionProcessor getExecutionProcessorAtCursor(Editor editor) {
        DBLanguagePsiFile file = (DBLanguagePsiFile) DocumentUtil.getFile(editor);
        String selection = editor.getSelectionModel().getSelectedText();
        if (selection != null) {
            return new StatementExecutionCursorProcessor(file, selection, getNextSequence());
        }

        ExecutablePsiElement executablePsiElement = PsiUtil.lookupExecutableAtCaret(editor, true);
        if (executablePsiElement != null) {
            return getExecutionProcessor(executablePsiElement, false);
        }

        return null;
    }

    public List<StatementExecutionProcessor> getExecutionProcessors(DBLanguagePsiFile file, int offset) {
        List<StatementExecutionProcessor> statements = new ArrayList<StatementExecutionProcessor>();

        PsiElement child = file.getFirstChild();
        while (child != null) {
            if (child instanceof RootPsiElement) {
                RootPsiElement root = (RootPsiElement) child;

                for (ExecutablePsiElement executable: root.getExecutablePsiElements()) {
                    if (executable.getTextOffset() > offset) {
                        StatementExecutionProcessor executionProcessor = getExecutionProcessor(executable, true);
                        statements.add(executionProcessor);
                    }
                }
            }
            child = child.getNextSibling();
        }
        return statements;
    }

    @Nullable
    public StatementExecutionProcessor getExecutionProcessor(ExecutablePsiElement executablePsiElement, boolean create) {
        synchronized(executionProcessors) {
            cleanup();
            StatementExecutionProcessor executionProcessor = findExecutionProcessor(executablePsiElement, false);
            if (executionProcessor == null) {
                executionProcessor = findExecutionProcessor(executablePsiElement, true);
            }
            if (executionProcessor == null && create) {
                executionProcessor = createExecutionProcessor(executablePsiElement);
            }
            return executionProcessor;
        }
    }

    private StatementExecutionProcessor findExecutionProcessor(ExecutablePsiElement executablePsiElement, boolean lenient) {
        for (StatementExecutionProcessor executionProcessor : executionProcessors) {
            if (executionProcessor.getExecutablePsiElement().matches(executablePsiElement, lenient)) {
                return executionProcessor;
            }
        }
        return null;
    }

    public StatementExecutionProcessor createExecutionProcessor(ExecutablePsiElement executablePsiElement) {
        synchronized(executionProcessors) {
            StatementExecutionBasicProcessor executionProcessor =
                    executablePsiElement.isQuery() ?
                            new StatementExecutionCursorProcessor(executablePsiElement, getNextSequence()) :
                            new StatementExecutionBasicProcessor(executablePsiElement, getNextSequence());
            executionProcessors.add(executionProcessor);
            return executionProcessor;
        }
    }


    private void cleanup() {
        synchronized(executionProcessors) {
            Iterator<StatementExecutionProcessor> executionProcessorIterator = executionProcessors.iterator();
            while (executionProcessorIterator.hasNext()) {
                StatementExecutionProcessor executionProcessor = executionProcessorIterator.next();
                if (executionProcessor.isDisposed()) {
                    executionProcessorIterator.remove();
                }
            }
            if (executionProcessors.size() == 0) {
                sequence = 0;
            }
        }
    }

    /*********************************************************
     *                    ProjectComponent                   *
     *********************************************************/
    @NotNull
    @NonNls
    public String getComponentName() {
        return "DBNavigator.Project.StatementExecutionManager";
    }
}
