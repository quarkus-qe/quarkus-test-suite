package io.quarkus.ts.transactions.recovery.service;

import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;

import io.quarkus.ts.transactions.recovery.TransactionExecutor;
import io.quarkus.ts.transactions.recovery.TransactionRecoveryService;

@ApplicationScoped
public class InjectedUserTransactionRecoveryService extends TransactionRecoveryService {

    @Inject
    UserTransaction userTransaction;

    @Override
    public TransactionExecutor transactionExecutor() {
        return TransactionExecutor.INJECTED_USER_TRANSACTION;
    }

    @Override
    protected <T> T withTransaction(Supplier<T> runInsideTransaction) {
        try {
            userTransaction.begin();
            var result = runInsideTransaction.get();
            userTransaction.commit();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected <T> T withTransactionAndRollback(Supplier<T> runInsideTransaction) {
        try {
            userTransaction.begin();
            var result = runInsideTransaction.get();
            userTransaction.rollback();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
