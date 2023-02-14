package io.quarkus.ts.transactions;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;

@Entity(name = "journal")
public class JournalEntity extends PanacheEntity {

    @Column(nullable = false)
    private String annotation;
    @Column(nullable = false)
    private String accountTo;
    @Column(nullable = false)
    private String accountFrom;
    @Column(nullable = false)
    private int amount;
    @Column(nullable = false)
    private Timestamp createdAt;

    public JournalEntity() {
    }

    public JournalEntity(String accountFrom, String accountTo, String annotation, int amount) {
        this.accountFrom = accountFrom;
        this.accountTo = accountTo;
        this.annotation = annotation;
        this.amount = amount;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public JournalEntity addLog() {
        persistAndFlush();
        return this;
    }

    public static JournalEntity getLatestJournalRecord(String accountNumber) {
        return find("accountFrom = :accountFrom",
                Sort.by("createdAt").descending(),
                Parameters.with("accountFrom", accountNumber))
                .firstResult();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public String getAccountTo() {
        return accountTo;
    }

    public void setAccountTo(String accountTo) {
        this.accountTo = accountTo;
    }

    public String getAccountFrom() {
        return accountFrom;
    }

    public void setAccountFrom(String accountFrom) {
        this.accountFrom = accountFrom;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
