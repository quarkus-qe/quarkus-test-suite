package io.quarkus.ts.transactions.recovery.service;

import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.ts.transactions.recovery.TransactionExecutor;
import io.quarkus.ts.transactions.recovery.TransactionRecoveryService;

@ApplicationScoped
public class QuarkusTransactionCallRecoveryService extends TransactionRecoveryService {

    @Override
    public TransactionExecutor transactionExecutor() {
        return TransactionExecutor.QUARKUS_TRANSACTION_CALL;
    }

    @Override
    protected <T> T withTransaction(Supplier<T> runInsideTransaction) {
        return QuarkusTransaction.requiringNew().call(runInsideTransaction::get);
    }

    @Override
    protected <T> T withTransactionAndRollback(Supplier<T> runInsideTransaction) {
        return QuarkusTransaction.requiringNew().call(() -> {
            var result = runInsideTransaction.get();
            QuarkusTransaction.rollback();
            return result;
        });
    }

}
