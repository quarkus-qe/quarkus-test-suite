package io.quarkus.ts.transactions;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Sort;

@Entity(name = "client")
public class ClientEntity extends PanacheEntity {

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String lastName;
    @Column(unique = true, nullable = false, name = "account_number")
    private String accountNumber;

    public ClientEntity() {
    }

    public ClientEntity(String name, String lastName, String accountNumber) {
        this.name = name;
        this.lastName = lastName;
        this.accountNumber = accountNumber;
    }

    public static ClientEntity findClient(String accountNumber) {
        return find("accountNumber", accountNumber).firstResult();
    }

    public static List<ClientEntity> getAllclients() {
        return findAll(Sort.by("name").descending()).list();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}
