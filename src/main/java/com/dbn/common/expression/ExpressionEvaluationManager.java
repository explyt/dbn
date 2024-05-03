package com.dbn.common.expression;

import com.dbn.DatabaseNavigator;
import com.dbn.common.component.Components;
import com.dbn.common.component.PersistentState;
import com.dbn.common.component.ProjectComponentBase;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.script.ScriptException;
import java.util.Map;

@Slf4j
@State(
        name = ExpressionEvaluationManager.COMPONENT_NAME,
        storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class ExpressionEvaluationManager extends ProjectComponentBase implements PersistentState {

    public static final String COMPONENT_NAME = "DBNavigator.Project.ExpressionEvaluationManager";
    private final GroovyExpressionEvaluator expressionEvaluator = new GroovyExpressionEvaluator();

    private ExpressionEvaluationManager(Project project) {
        super(project, COMPONENT_NAME);
    }

    public static ExpressionEvaluationManager getInstance(@NotNull Project project) {
        return Components.projectService(project, ExpressionEvaluationManager.class);
    }

    @Override
    public Element getComponentState() {
        return null;
    }

    @Override
    public void loadComponentState(@NotNull final Element element) {
    }

    @Override
    public void disposeInner() {
        super.disposeInner();
    }
}