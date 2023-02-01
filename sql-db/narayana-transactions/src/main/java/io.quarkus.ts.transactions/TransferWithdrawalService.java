package io.quarkus.ts.transactions;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.jboss.logging.Logger;

import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.narayana.jta.QuarkusTransaction;

@ApplicationScoped
@Named("TransferWithdrawalService")
public class TransferWithdrawalService extends TransferProcessor {

    private static final Logger LOG = Logger.getLogger(TransferWithdrawalService.class);
    private final static int TRANSACTION_TIMEOUT_SEC = 10;
    private final static String ANNOTATION_WITHDRAWAL = "user withdrawal";
    private final MeterRegistry registry;
    private long transactionsAmount;

    public TransferWithdrawalService(MeterRegistry registry) {
        this.registry = registry;
        registry.gauge("transaction.withdrawal.amount", this, TransferWithdrawalService::getTransactionsAmount);
    }

    public JournalEntity makeTransaction(String from, String to, int amount) {
        JournalEntity journal = null;
        try {
            LOG.infof("Withdrawal account %s amount %s", from, amount);
            // please don't move this gauge after commit statement, because we want to test the gauges after a rollback
            transactionsAmount++;
            verifyAccounts(from);
            QuarkusTransaction.begin(QuarkusTransaction.beginOptions().timeout(TRANSACTION_TIMEOUT_SEC));
            journal = journalService.addToJournal(from, to, ANNOTATION_WITHDRAWAL, amount);
            accountService.decreaseBalance(from, amount);
            QuarkusTransaction.commit();
            LOG.infof("Withdrawal completed account %s", from);
        } catch (Exception e) {
            LOG.errorf("Error on withdrawal transaction %s ", e.getMessage());
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
