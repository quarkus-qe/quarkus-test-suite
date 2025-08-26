package io.quarkus.ts.transactions.recovery.service;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.narayana.jta.TransactionExceptionResult;
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
        AtomicReference<T> result = new AtomicReference<>();
        try {
            // on the exception, we expect that the "new" transaction gets rolled back
            // and later when the test queries database, the result must not be there
            return QuarkusTransaction
                    .requiringNew()
                    .exceptionHandler(t -> TransactionExceptionResult.ROLLBACK)
                    .call(() -> {
                        result.set(runInsideTransaction.get());
                        throw new RuntimeException("Raising exception in order to force rollback");
                    });
        } catch (Exception e) {
            // this is just what we do for other transaction recovery services because there, we don't
            // need to raise exceptions
            return result.get();
        }
    }

}
