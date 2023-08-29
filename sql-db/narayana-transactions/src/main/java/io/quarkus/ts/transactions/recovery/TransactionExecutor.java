package io.quarkus.ts.transactions.recovery;

public enum TransactionExecutor {
    TRANSACTIONAL_ANNOTATION(100),
    STATIC_TRANSACTION_MANAGER(1000),
    INJECTED_TRANSACTION_MANAGER(10000),
    INJECTED_USER_TRANSACTION(100000),
    STATIC_USER_TRANSACTION(1000000),
    QUARKUS_TRANSACTION(10000000),
    QUARKUS_TRANSACTION_CALL(100000000);

    final int idOffset;

    TransactionExecutor(int idOffset) {
        this.idOffset = idOffset;
    }
}
