package io.quarkus.ts.transactions;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import io.agroal.api.AgroalDataSource;
import io.quarkus.agroal.DataSource;

@ApplicationScoped
public class ClientService {

    @Inject
    @DataSource("xa-ds-1")
    AgroalDataSource agroalDataSource;

    public List<ClientEntity> getAllClients() {
        return ClientEntity.getAllclients();
    }

    public ClientEntity getAccount(String accountNumber) {
        return ClientEntity.findClient(accountNumber);
    }

    public void createAccount(ClientEntity account) {
        ClientEntity.persist(account);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void createAccountManually(ClientEntity client) {
        try (var connection = agroalDataSource.getConnection()) {
            String prepareStatement = "INSERT INTO client (id, name, lastName, account_number) VALUES (3, ?, ?, ?)";
            try (var statement = connection.prepareStatement(prepareStatement)) {
                statement.setString(1, client.getName());
                statement.setString(2, client.getLastName());
                statement.setString(3, client.getAccountNumber());
                statement.execute();
            }
        } catch (Exception e) {
            throw new RuntimeException("Database operation failed", e);
        }
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void deleteAccountManually(String accountNumber) {
        try (var connection = agroalDataSource.getConnection()) {
            String prepareStatement = "DELETE FROM client WHERE account_number = ?";
            try (var statement = connection.prepareStatement(prepareStatement)) {
                statement.setString(1, accountNumber);
                statement.execute();
            }
        } catch (Exception e) {
            throw new RuntimeException("Database operation failed", e);
        }
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void updateAccountManually(String newName, String accountNumber) {
        try (var connection = agroalDataSource.getConnection()) {
            String prepareStatement = "UPDATE client SET name = ? WHERE account_number = ?";
            try (var statement = connection.prepareStatement(prepareStatement)) {
                statement.setString(1, newName);
                statement.setString(2, accountNumber);
                statement.execute();
            }
        } catch (Exception e) {
            throw new RuntimeException("Database operation failed", e);
        }
    }
}
