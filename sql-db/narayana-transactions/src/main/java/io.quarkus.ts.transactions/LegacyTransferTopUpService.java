package io.quarkus.ts.transactions;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import org.jboss.logging.Logger;

import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.narayana.jta.RunOptions;

@SuppressWarnings("deprecation") //  this class intentionally uses deprecated apis
@ApplicationScoped
@Named("LegacyTransferTopUpService")
public class LegacyTransferTopUpService extends TransferProcessor {
    private static final Logger LOG = Logger.getLogger(LegacyTransferTopUpService.class);
    private final static String ANNOTATION_TOP_UP = "user top up";
    private final static int TRANSACTION_TIMEOUT_SEC = 10;
    private final MeterRegistry registry;
    private long transactionsAmount;

    public LegacyTransferTopUpService(MeterRegistry registry) {
        this.registry = registry;
        registry.gauge("transaction.topup.amount", this, LegacyTransferTopUpService::getTransactionsAmount);
    }

    public JournalEntity makeTransaction(String from, String to, int amount) {
        LOG.infof("TopUp account %s amount %s", from, amount);
        verifyAccounts(to);
        JournalEntity journal = QuarkusTransaction.call(QuarkusTransaction.runOptions()
                .timeout(TRANSACTION_TIMEOUT_SEC)
                .exceptionHandler(t -> {
                    transactionsAmount--;
                    return RunOptions.ExceptionResult.ROLLBACK;
                })
                .semantic(RunOptions.Semantic.REQUIRE_NEW), () -> {
                    transactionsAmount++;
                    JournalEntity journalentity = journalService.addToJournal(from, to, ANNOTATION_TOP_UP, amount);
                    accountService.increaseBalance(from, amount);
                    return journalentity;
                });
        LOG.infof("TopUp completed account %s", from);
        return journal;
    }

    public long getTransactionsAmount() {
        return transactionsAmount;
    }
}
