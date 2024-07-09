package com.dbn.execution.logging;

import com.dbn.common.locale.Formatter;
import com.dbn.connection.ConnectionHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.Data;
import lombok.Getter;

import java.util.Date;

@Data
public class LogOutput {

    @Getter
    public enum Type {
        SYS(ProcessOutputTypes.SYSTEM),
        STD(ProcessOutputTypes.STDOUT),
        ERR(ProcessOutputTypes.STDERR);
        private final Key<?> key;

        Type(Key<?> key) {
            this.key = key;
        }
    }

    private final String text;
    private final Type type;
    private final boolean scrollToEnd;
    private final boolean clearBuffer;

    private LogOutput(String text, Type type) {
        this(text, type, false, false);
    }
    private LogOutput(String text, Type type, boolean scrollToEnd, boolean clearBuffer) {
        this.text = text;
        this.type = type;
        this.scrollToEnd = scrollToEnd;
        this.clearBuffer = clearBuffer;
    }

    public static LogOutput createErrOutput(String text) {
        return new LogOutput(text, Type.ERR);
    }

    public static LogOutput createStdOutput(String text) {
        return new LogOutput(text, Type.STD);
    }

    public static LogOutput createSysOutput(String text) {
        return new LogOutput(text, Type.SYS);
    }

    public static LogOutput createSysOutput(LogOutputContext context, String message, boolean clearBuffer) {
        return createSysOutput(context, System.currentTimeMillis(), message, clearBuffer);
    }

    public static LogOutput createSysOutput(LogOutputContext context, long timestamp, String message, boolean clearBuffer) {
        ConnectionHandler connection = context.getConnection();
        Project project = connection.getProject();
        Formatter formatter = Formatter.getInstance(project);
        String date = formatter.formatDateTime(new Date(timestamp));
        String text = date + ": " + connection.getName();
        VirtualFile sourceFile = context.getSourceFile();
        if (sourceFile != null) {
            text += " / " + sourceFile.getName();
        }
        text += message;

        return new LogOutput(text, Type.SYS, true, clearBuffer);
    }

}
