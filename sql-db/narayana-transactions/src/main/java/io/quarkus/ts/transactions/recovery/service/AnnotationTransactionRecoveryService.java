package io.quarkus.ts.transactions.recovery.service;

import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Transactional;

import io.quarkus.ts.transactions.recovery.TransactionExecutor;
import io.quarkus.ts.transactions.recovery.TransactionRecoveryService;

@ApplicationScoped
public class AnnotationTransactionRecoveryService extends TransactionRecoveryService {

    @Inject
    TransactionManager manager;

    @Override
    public TransactionExecutor transactionExecutor() {
        return TransactionExecutor.TRANSACTIONAL_ANNOTATION;
    }

    @Transactional
    @Override
    protected <T> T withTransaction(Supplier<T> runInsideTransaction) {
        return runInsideTransaction.get();
    }

    @Transactional
    @Override
    protected <T> T withTransactionAndRollback(Supplier<T> runInsideTransaction) {
        var result = runInsideTransaction.get();
        try {
            manager.getTransaction().setRollbackOnly();
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

}
