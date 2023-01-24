package io.quarkus.ts.transactions;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;

@Entity(name = "account")
public class AccountEntity extends PanacheEntity {
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String lastName;
    @Column(unique = true, nullable = false)
    private String accountNumber;
    @Column(precision = 10, scale = 2, nullable = false)
    private int amount;
    private Timestamp updatedAt;
    @Column(nullable = false)
    private Timestamp createdAt;

    public static boolean exist(String accountNumber) {
        return Objects.nonNull(findAccount(accountNumber));
    }

    public static AccountEntity findAccount(String accountNumber) {
        return find("accountNumber", accountNumber).firstResult();
    }

    public static int updateAmount(String accountNumber, int amount) {
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        int updatedRecordsAmount = update("amount = :amount, updatedAt = :updatedAt where accountNumber = :account",
                Parameters.with("amount", amount)
                        .and("updatedAt", currentTime)
                        .and("account", accountNumber));
        flush();
        return updatedRecordsAmount;
    }

    public static List<AccountEntity> getAllAccountsRecords() {
        return findAll(Sort.by("createdAt").descending()).list();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}
