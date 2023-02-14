package io.quarkus.ts.transactions;

import static io.quarkus.ts.transactions.JournalEntity.getLatestJournalRecord;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JournalService {

    public JournalEntity addToJournal(String accountFrom, String accountTo, String annotation, int amount) {
        JournalEntity journal = new JournalEntity(accountFrom, accountTo, annotation, amount);
        return journal.addLog();
    }

    public JournalEntity getLatestJournalRecordByAccountNumber(String accountNumber) {
        return getLatestJournalRecord(accountNumber);
    }
}
