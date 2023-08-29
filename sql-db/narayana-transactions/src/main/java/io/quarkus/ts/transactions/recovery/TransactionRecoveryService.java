package io.quarkus.ts.transactions.recovery;

import java.sql.SQLException;
import java.util.function.Supplier;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import javax.sql.DataSource;

public abstract class TransactionRecoveryService {

    @Named("xa-ds-1")
    @Inject
    DataSource xaDataSource1;

    @Named("xa-ds-2")
    @Inject
    DataSource xaDataSource2;

    public int makeTransaction(boolean rollback) {
        if (rollback) {
            return withTransactionAndRollback(this::makeTransactionInternal);
        }
        return withTransaction(this::makeTransactionInternal);
    }

    public abstract TransactionExecutor transactionExecutor();

    protected abstract <T> T withTransaction(Supplier<T> runInsideTransaction);

    protected abstract <T> T withTransactionAndRollback(Supplier<T> runInsideTransaction);

    private int idOffset() {
        return transactionExecutor().idOffset;
    }

    private int makeTransactionInternal() {
        return makeTransactionInternal(0, idOffset(), xaDataSource1, xaDataSource2);
    }

    private static int makeTransactionInternal(int idx, int idOffset, DataSource... dataSources) {
        try (var con = dataSources[idx].getConnection()) {
            try (var statement = con.createStatement()) {
                int pk = idOffset + idx++;
                var result = statement.executeUpdate("INSERT INTO recovery_log (id) VALUES (" + pk + ")");
                if (idx < dataSources.length) {
                    return result + makeTransactionInternal(idx, idOffset, dataSources);
                } else {
                    return result;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
