package com.dbn.database.interfaces;

import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.nls.NlsSupport;

import java.sql.SQLException;

public interface DatabaseInterface extends NlsSupport {

    default void reset() {
    }

    interface Callable<T> {
        T call() throws SQLException;
    }

    interface Runnable {
        void run() throws SQLException;
    }

    interface ConnectionCallable<T> {
        T call(DBNConnection conn) throws SQLException;
    }

    interface ConnectionRunnable {
        void run(DBNConnection conn) throws SQLException;
    }

}
