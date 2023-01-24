package io.quarkus.ts.transactions;

import static javax.ws.rs.core.Response.Status.CREATED;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/transfer")
public class TransferResource {

    @Inject
    @Named("TransferTransactionService")
    TransferProcessor regularTransaction;

    @Inject
    @Named("TransferTopUpService")
    TransferProcessor topUp;

    @Inject
    @Named("TransferWithdrawalService")
    TransferProcessor withdrawal;

    @Inject
    JournalService journalService;

    @Inject
    AccountService accountService;

    /**
     * Transaction represent a transfer funds from one account to another account
     * On the journal will look like a transaction with different from / to accounts and the annotation transaction.
     */
    @Path("/transaction")
    @POST
    public Response makeRegularTransaction(TransferDTO transferDTO) {
        Long ID = regularTransaction.makeTransaction(transferDTO.getAccountFrom(), transferDTO.getAccountTo(),
                transferDTO.getAmount()).getId();

        return Response.ok(ID).status(CREATED.getStatusCode()).build();
    }

    /**
     * TopUp represent a transfer funds transaction to your account, but the money doesn't come from another account.
     * On the journal will look like a transaction with the same from / to account and the annotation top-up.
     */
    @Path("/top-up")
    @POST
    public Response topup(TransferDTO transferDTO) {
        Long ID = topUp.makeTransaction(transferDTO.getAccountFrom(), transferDTO.getAccountTo(),
                transferDTO.getAmount()).getId();

        return Response.ok(ID).status(CREATED.getStatusCode()).build();
    }

    /**
     * Withdrawal represent a take off funds from your account, but you don't transfer this money to another account
     * On the journal will look like a transaction with the same from / to accounts and the annotation withdrawal.
     */
    @Path("/withdrawal")
    @POST
    public Response makeMoneyTransaction(TransferDTO transferDTO) {
        Long ID = withdrawal.makeTransaction(transferDTO.getAccountFrom(), transferDTO.getAccountTo(),
                transferDTO.getAmount()).getId();

        return Response.ok(ID).status(CREATED.getStatusCode()).build();
    }

    @Path("/accounts/")
    @GET
    public List<AccountEntity> getAccounts() {
        return accountService.getAllAccounts();
    }

    @Path("/accounts/{account_id}")
    @GET
    public AccountEntity getAccountById(@PathParam("account_id") String accountNumber) {
        return accountService.getAccount(accountNumber);
    }

    @Path("/journal/latest/{account_id}")
    @GET
    public JournalEntity getLatestJournalRecord(@PathParam("account_id") String accountNumber) {
        return journalService.getLatestJournalRecordByAccountNumber(accountNumber);
    }

}
