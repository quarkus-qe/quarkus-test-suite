package io.quarkus.ts.transactions;

import io.quarkus.ts.transactions.recovery.TransactionExecutor;

public abstract class AbstractPostgresqlTransactionGeneralUsageIT extends TransactionCommons {

    static final int POSTGRESQL_PORT = 5432;

    @Override
    protected TransactionExecutor getTransactionExecutorUsedForRecovery() {
        return TransactionExecutor.TRANSACTIONAL_ANNOTATION;
    }
}
