package io.quarkus.ts.transactions.recovery.service;

import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.TransactionManager;

import io.quarkus.ts.transactions.recovery.TransactionExecutor;
import io.quarkus.ts.transactions.recovery.TransactionRecoveryService;

@ApplicationScoped
public class InjectedTransactionManagerRecoveryService extends TransactionRecoveryService {

    @Inject
    TransactionManager transactionManager;

    @Override
    public TransactionExecutor transactionExecutor() {
        return TransactionExecutor.INJECTED_TRANSACTION_MANAGER;
    }

    @Override
    protected <T> T withTransaction(Supplier<T> runInsideTransaction) {
        try {
            transactionManager.begin();
            var result = runInsideTransaction.get();
            transactionManager.commit();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected <T> T withTransactionAndRollback(Supplier<T> runInsideTransaction) {
        try {
            transactionManager.begin();
            var result = runInsideTransaction.get();
            transactionManager.rollback();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
