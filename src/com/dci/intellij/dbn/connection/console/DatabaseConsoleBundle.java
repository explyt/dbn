package com.dci.intellij.dbn.connection.console;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
import com.intellij.openapi.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatabaseConsoleBundle implements Disposable{
    private ConnectionHandler connectionHandler;

    private List<DBConsoleVirtualFile> consoles = new ArrayList<DBConsoleVirtualFile>();

    public DatabaseConsoleBundle(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    public List<DBConsoleVirtualFile> getConsoles() {
        if (consoles.size() == 0) {
            createConsole(connectionHandler.getName());
        }
        return consoles;
    }

    public Set<String> getConsoleNames() {
        Set<String> consoleNames = new HashSet<String>();
        for (DBConsoleVirtualFile console : consoles) {
            consoleNames.add(console.getName());
        }

        return consoleNames;
    }

    @NotNull
    public DBConsoleVirtualFile getDefaultConsole() {
        return getConsole(connectionHandler.getName(), true);
    }

    @Nullable
    public DBConsoleVirtualFile getConsole(String name) {
        return getConsole(name, false);
    }

    private DBConsoleVirtualFile getConsole(String name, boolean create) {
        for (DBConsoleVirtualFile console : consoles) {
            if (console.getName().equals(name)) {
                return console;
            }
        }
        return create ? createConsole(name) : null;
    }

    public DBConsoleVirtualFile createConsole(String name) {
        DBConsoleVirtualFile console = new DBConsoleVirtualFile(connectionHandler, name);
        consoles.add(console);
        Collections.sort(consoles);
        return console;
    }

    public void removeConsole(String name) {
        DBConsoleVirtualFile console = getConsole(name);
        if (console != null) {
            consoles.remove(console);
            console.release();
        }
    }

    @Override
    public void dispose() {
        for (DBConsoleVirtualFile console : consoles) {
            console.release();
        }
        consoles.clear();
    }

    public void renameConsole(String oldName, String newName) {
        DBConsoleVirtualFile console = getConsole(oldName);
        if (console != null) {
            console.setName(newName);
        }
    }
}
