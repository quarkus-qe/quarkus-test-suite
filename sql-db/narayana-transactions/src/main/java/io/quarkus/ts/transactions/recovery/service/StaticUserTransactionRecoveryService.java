package io.quarkus.ts.transactions.recovery.service;

import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;

import com.arjuna.ats.jta.UserTransaction;

import io.quarkus.ts.transactions.recovery.TransactionExecutor;
import io.quarkus.ts.transactions.recovery.TransactionRecoveryService;

@ApplicationScoped
public class StaticUserTransactionRecoveryService extends TransactionRecoveryService {

    @Override
    public TransactionExecutor transactionExecutor() {
        return TransactionExecutor.STATIC_USER_TRANSACTION;
    }

    @Override
    protected <T> T withTransaction(Supplier<T> runInsideTransaction) {
        try {
            UserTransaction.userTransaction().begin();
            var result = runInsideTransaction.get();
            UserTransaction.userTransaction().commit();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected <T> T withTransactionAndRollback(Supplier<T> runInsideTransaction) {
        try {
            UserTransaction.userTransaction().begin();
            var result = runInsideTransaction.get();
            UserTransaction.userTransaction().rollback();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
