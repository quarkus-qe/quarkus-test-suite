package io.quarkus.ts.transactions.recovery.service;

import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;

import com.arjuna.ats.jta.TransactionManager;

import io.quarkus.ts.transactions.recovery.TransactionExecutor;
import io.quarkus.ts.transactions.recovery.TransactionRecoveryService;

@ApplicationScoped
public class StaticTransactionManagerRecoveryService extends TransactionRecoveryService {

    @Override
    public TransactionExecutor transactionExecutor() {
        return TransactionExecutor.STATIC_TRANSACTION_MANAGER;
    }

    @Override
    protected <T> T withTransaction(Supplier<T> runInsideTransaction) {
        try {
            TransactionManager.transactionManager().begin();
            var result = runInsideTransaction.get();
            TransactionManager.transactionManager().commit();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected <T> T withTransactionAndRollback(Supplier<T> runInsideTransaction) {
        try {
            TransactionManager.transactionManager().begin();
            var result = runInsideTransaction.get();
            TransactionManager.transactionManager().rollback();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
