package io.quarkus.ts.transactions;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.jboss.logging.Logger;

import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.narayana.jta.QuarkusTransaction;

@ApplicationScoped
@Named("TransferTransactionService")
public class TransferTransactionService extends TransferProcessor {

    private static final Logger LOG = Logger.getLogger(TransferTransactionService.class);
    private final static String ANNOTATION_TRANSACTION = "user transaction to other user";
    private final static int TRANSACTION_TIMEOUT_SEC = 10;
    private final MeterRegistry registry;
    private long transactionsAmount;

    public TransferTransactionService(MeterRegistry registry) {
        this.registry = registry;
        registry.gauge("transaction.regular.amount", this, TransferTransactionService::getTransactionsAmount);
    }

    public JournalEntity makeTransaction(String from, String to, int amount) {
        JournalEntity journal = null;
        LOG.infof("Regular transaction, from %s to %s amount %s", from, to, amount);
        try {
            // please don't move this gauge after commit statement, because we want to test the gauges after a rollback
            transactionsAmount++;
            verifyAccounts(from, to);
            QuarkusTransaction.begin(QuarkusTransaction.beginOptions().timeout(TRANSACTION_TIMEOUT_SEC));
            journal = journalService.addToJournal(from, to, ANNOTATION_TRANSACTION, amount);
            accountService.decreaseBalance(from, amount);
            accountService.increaseBalance(to, amount);
            QuarkusTransaction.commit();
            LOG.infof("Regular transaction completed, from %s to %s", from, to);
        } catch (Exception e) {
            LOG.errorf("Error on regular transaction %s ", e.getMessage());
            QuarkusTransaction.rollback();
            transactionsAmount--;
            throw e;
        }

        return journal;
    }

    public long getTransactionsAmount() {
        return transactionsAmount;
    }
}
