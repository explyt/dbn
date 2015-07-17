package com.dci.intellij.dbn.debugger.jdwp.frame;

import javax.swing.Icon;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.thread.SimpleBackgroundTask;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.debugger.jdwp.DBJdwpDebugProcess;
import com.intellij.debugger.engine.JavaValue;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XNamedValue;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.intellij.xdebugger.frame.XValueModifier;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import com.sun.jdi.Field;
import com.sun.jdi.Value;
import com.sun.tools.jdi.ClassTypeImpl;
import com.sun.tools.jdi.ObjectReferenceImpl;

public class DBJdwpDebugValue extends XNamedValue implements Comparable<DBJdwpDebugValue>{
    private DBJdwpDebugValueModifier modifier;
    private String value;
    private String errorMessage;
    private Icon icon;
    private int frameIndex;
    private DBJdwpDebugValue parentValue;
    private Set<String> childVariableNames;
    private DBJdwpDebugStackFrame stackFrame;

    public DBJdwpDebugValue(DBJdwpDebugStackFrame stackFrame, DBJdwpDebugValue parentValue, String variableName, @Nullable Set<String> childVariableNames, Icon icon, int frameIndex) {
        super(variableName);
        this.stackFrame = stackFrame;
        if (icon == null) {
            icon = parentValue == null ?
                    Icons.DBO_VARIABLE :
                    Icons.DBO_ATTRIBUTE;
        }
        this.icon = icon;
        this.parentValue = parentValue;

        this.frameIndex = frameIndex;
        this.childVariableNames = childVariableNames;
    }

    public DBJdwpDebugProcess getDebugProcess() {
        return stackFrame.getDebugProcess();
    }

    public String getVariableName() {
        return getName();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void computePresentation(@NotNull final XValueNode node, @NotNull final XValuePlace place) {
        //node.setPresentation(icon, null, "", childVariableNames != null);
        new SimpleBackgroundTask("load variable value") {
            @Override
            protected void execute() {
                try {
                    String variableName = getVariableName();
                    String databaseVariableName = parentValue == null ? variableName : parentValue.getVariableName() + "." + variableName;

                    XStackFrame underlyingFrame = stackFrame.getUnderlyingFrame();
                    XDebuggerEvaluator evaluator = underlyingFrame.getEvaluator();
                    //node.setPresentation(icon, null, "", childVariableNames != null);
                    if (evaluator != null) {
                        XDebuggerEvaluator.XEvaluationCallback evaluationCallback = new XDebuggerEvaluator.XEvaluationCallback() {
                            @Override
                            public void evaluated(@NotNull XValue result) {
                                ObjectReferenceImpl value = (ObjectReferenceImpl) ((JavaValue) result).getDescriptor().getValue();
                                final List<Field> fields = ((ClassTypeImpl) value.type()).fields();
                                String stringValue = "null";
                                if (fields.size() > 0) {
                                    final Value value1 = value.getValue(fields.get(0));
                                    if  (value1 != null) {
                                        stringValue = value1.toString();
                                    }
                                }

                                //result.computePresentation(node, place);
                                node.setPresentation(icon, null, stringValue, childVariableNames != null);
                            }

                            @Override
                            public void errorOccurred(@NotNull String errorMessage) {
                                DBJdwpDebugValue.this.value = "";
                                DBJdwpDebugValue.this.errorMessage = "could not resolve variable";
                                node.setPresentation(icon, null, "", childVariableNames != null);
                            }
                        };
                        evaluator.evaluate(databaseVariableName.toUpperCase(), evaluationCallback, null);
                    }

/*                    Location location = stackFrame.getLocation();
                    List<LocalVariable> variables = location.method().variables();
                    for (LocalVariable variable : variables) {
                        if (variable.name().equalsIgnoreCase(databaseVariableName)) {
                            final XStackFrame underlyingFrame = stackFrame.getUnderlyingFrame();

                        }
                    }*/



/*
                    VariableInfo variableInfo = debugProcess.getDebuggerInterface().getVariableInfo(
                            databaseVariableName.toUpperCase(), frameIndex,
                            debugProcess.getDebugConnection());
                    value = variableInfo.getValue();
                    errorMessage = variableInfo.getError();
*/
                    if (childVariableNames != null) {
                        errorMessage = null;
                    }

                    if (value == null) {
                        value = childVariableNames != null ? "" : "null";
                    } else {
                        if (!StringUtil.isNumber(value)) {
                            value = '\'' + value + '\'';
                        }
                    }

                    if (errorMessage != null) {
                        errorMessage = errorMessage.toLowerCase();
                        value = "";
                    }
                    if (childVariableNames != null) {
                        errorMessage = "record";
                    }
                } catch (Exception e) {
                    value = "";
                    errorMessage = e.getMessage();
                } finally {
                    //node.setPresentation(icon, errorMessage, CommonUtil.nvl(value, "null"), childVariableNames != null);
                }

            }
        }.start();
    }

    @Override
    public XValueModifier getModifier() {
        if (modifier == null) modifier = new DBJdwpDebugValueModifier(this);
        return modifier;
    }

    public int compareTo(@NotNull DBJdwpDebugValue remote) {
        return getName().compareTo(remote.getName());
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        if (childVariableNames != null) {
            for (String childVariableName : childVariableNames) {
                childVariableName = childVariableName.substring(getVariableName().length() + 1);
                XValueChildrenList debugValueChildren = new XValueChildrenList();
                DBJdwpDebugValue value = new DBJdwpDebugValue(stackFrame, this, childVariableName, null, null, frameIndex);
                debugValueChildren.add(value);
                node.addChildren(debugValueChildren, true);
            }
        } else {
            super.computeChildren(node);
        }

    }

/*    private List<DBObject> getChildObjects() {
        DBObject object = DBObjectRef.get(objectRef);
        if (object instanceof DBVirtualObject) {
            DBObjectListContainer childObjectsContainer = object.getChildObjects();
            if (childObjectsContainer != null) {
                List<DBObjectList<DBObject>> objectLists = childObjectsContainer.getAllObjectLists();
                if (objectLists.size() > 0) {
                    return objectLists.get(0).getObjects();
                }
            }
        }
        return null;
    }*/
}
