package io.quarkus.ts.transactions;

import javax.inject.Inject;

public abstract class TransferProcessor {

    @Inject
    AccountService accountService;

    @Inject
    JournalService journalService;

    protected void verifyAccounts(String... accounts) {
        for (String account : accounts) {
            accountService.isPresent(account);
        }
    }

    public abstract JournalEntity makeTransaction(String from, String to, int amount);
}
