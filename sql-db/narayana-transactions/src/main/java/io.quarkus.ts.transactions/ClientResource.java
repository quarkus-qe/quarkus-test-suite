package io.quarkus.ts.transactions;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.quarkus.narayana.jta.runtime.TransactionConfiguration;

@Path("/client")
public class ClientResource {

    @Inject
    ClientService clientService;

    @POST
    @Path("/create")
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    @TransactionConfiguration(timeout = 1)
    public Response createClient(ClientEntity client) {
        clientService.createAccount(client);
        return Response.ok(client).status(201).build();
    }

    @POST
    @Path("/create-manually")
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    @TransactionConfiguration(timeout = 1)
    public Response createClientManually(ClientEntity client) throws InterruptedException {
        Thread.sleep(2000);
        clientService.createAccountManually(client);
        return Response.ok(client).status(201).build();
    }

    @DELETE
    @Path("/delete/{account_number}")
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    @TransactionConfiguration(timeout = 1)
    public Response deleteClientManually(@PathParam("account_number") String accountNumber) throws InterruptedException {
        Thread.sleep(2000);
        clientService.deleteAccountManually(accountNumber);
        return Response.ok("Client deleted").status(204).build();
    }

    @PATCH
    @Path("/update/{account_number}")
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    @TransactionConfiguration(timeout = 1)
    public Response updateClientManually(@PathParam("account_number") String accountNumber, @QueryParam("name") String newName)
            throws InterruptedException {
        Thread.sleep(2000);
        clientService.updateAccountManually(newName, accountNumber);
        return Response.ok("Client updated").status(200).build();
    }

    @Path("/all")
    @GET
    public List<ClientEntity> getAccounts() {
        return clientService.getAllClients();
    }

    @Path("/{account_number}")
    @GET
    public ClientEntity getClientByAccountNumber(@PathParam("account_number") String accountNumber) {
        return clientService.getAccount(accountNumber);
    }
}
