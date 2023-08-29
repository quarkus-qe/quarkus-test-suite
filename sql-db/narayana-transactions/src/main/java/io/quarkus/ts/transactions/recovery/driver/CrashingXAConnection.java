package io.quarkus.ts.transactions.recovery.driver;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.ConnectionEventListener;
import javax.sql.StatementEventListener;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

public class CrashingXAConnection implements XAConnection {

    private final XAConnection delegate;

    CrashingXAConnection(XAConnection delegate) {
        this.delegate = delegate;
    }

    @Override
    public XAResource getXAResource() throws SQLException {
        return new CrashingXAResource(delegate.getXAResource());
    }

    @Override
    public Connection getConnection() throws SQLException {
        return delegate.getConnection();
    }

    @Override
    public void close() throws SQLException {
        delegate.close();
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        delegate.addConnectionEventListener(listener);
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        delegate.removeConnectionEventListener(listener);
    }

    @Override
    public void addStatementEventListener(StatementEventListener listener) {
        delegate.addStatementEventListener(listener);
    }

    @Override
    public void removeStatementEventListener(StatementEventListener listener) {
        delegate.removeStatementEventListener(listener);
    }
}
