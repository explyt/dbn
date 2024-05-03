package com.dbn.execution.common.message.ui.tree.node;

import com.dbn.execution.common.message.ui.tree.MessagesTreeLeafNode;
import com.dbn.execution.compiler.CompilerMessage;
import com.dbn.execution.compiler.CompilerResult;
import com.dbn.vfs.file.DBContentVirtualFile;
import org.jetbrains.annotations.Nullable;

public class CompilerMessageNode extends MessagesTreeLeafNode<CompilerMessagesObjectNode, CompilerMessage> {

    CompilerMessageNode(CompilerMessagesObjectNode parent, CompilerMessage compilerMessage) {
        super(parent, compilerMessage);
    }

    @Nullable
    @Override
    public DBContentVirtualFile getFile() {
        return getMessage().getContentFile();
    }

    @Override
    public String toString() {
        CompilerMessage compilerMessage = getMessage();
        return "[" + compilerMessage.getType() + "] " + compilerMessage.getText();
    }
}