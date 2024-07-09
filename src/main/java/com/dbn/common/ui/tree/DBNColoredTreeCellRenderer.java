package com.dbn.common.ui.tree;

import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.ui.ColoredTreeCellRenderer;

import javax.swing.*;

import static com.dbn.diagnostics.Diagnostics.conditionallyLog;

public abstract class DBNColoredTreeCellRenderer extends ColoredTreeCellRenderer {
    @Override
    public final void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        try {
            DBNTree dbnTree = (DBNTree) tree;
            customizeCellRenderer(dbnTree, value, selected, expanded, leaf, row, hasFocus);
        } catch (ProcessCanceledException e){
            conditionallyLog(e);
        } catch (IllegalStateException | AbstractMethodError e){
            conditionallyLog(e);
        }
    }

    protected abstract void customizeCellRenderer(DBNTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus);
}
