package io.quarkus.ts.transactions.recovery.service;

import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.ts.transactions.recovery.TransactionExecutor;
import io.quarkus.ts.transactions.recovery.TransactionRecoveryService;

@ApplicationScoped
public class QuarkusTransactionRecoveryService extends TransactionRecoveryService {

    @Override
    public TransactionExecutor transactionExecutor() {
        return TransactionExecutor.QUARKUS_TRANSACTION;
    }

    @Override
    protected <T> T withTransaction(Supplier<T> runInsideTransaction) {
        QuarkusTransaction.begin();
        var result = runInsideTransaction.get();
        QuarkusTransaction.commit();
        return result;
    }

    @Override
    protected <T> T withTransactionAndRollback(Supplier<T> runInsideTransaction) {
        QuarkusTransaction.begin();
        var result = runInsideTransaction.get();
        QuarkusTransaction.rollback();
        return result;
    }

}
