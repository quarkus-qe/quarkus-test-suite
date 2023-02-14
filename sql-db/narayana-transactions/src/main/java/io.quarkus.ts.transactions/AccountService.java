package io.quarkus.ts.transactions;

import static io.quarkus.ts.transactions.AccountEntity.exist;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import org.jboss.logging.Logger;

@ApplicationScoped
public class AccountService {

    private static final Logger LOG = Logger.getLogger(AccountService.class);

    public boolean isPresent(String accountNumber) {
        if (!exist(accountNumber)) {
            String msg = String.format("Account %s doesn't exist", accountNumber);
            LOG.warn(msg);
            throw new NotFoundException(msg);
        }

        return true;
    }

    public int increaseBalance(String account, int amount) {
        AccountEntity accountEntity = AccountEntity.findAccount(account);
        int updatedAmount = accountEntity.getAmount() + amount;
        AccountEntity.updateAmount(account, updatedAmount);
        return AccountEntity.findAccount(account).getAmount();
    }

    public int decreaseBalance(String account, int amount) {
        AccountEntity accountEntity = AccountEntity.findAccount(account);
        int updatedAmount = accountEntity.getAmount() - amount;
        if (updatedAmount < 0) {
            String msg = String.format("Account %s Not enough balance.", account);
            LOG.warn(msg);
            throw new BadRequestException(msg);
        }
        AccountEntity.updateAmount(account, updatedAmount);
        return updatedAmount;
    }

    public List<AccountEntity> getAllAccounts() {
        return AccountEntity.getAllAccountsRecords();
    }

    public AccountEntity getAccount(String accountNumber) {
        return AccountEntity.findAccount(accountNumber);
    }
}
