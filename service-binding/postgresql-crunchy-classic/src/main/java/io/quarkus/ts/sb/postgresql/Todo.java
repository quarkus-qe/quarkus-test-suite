package io.quarkus.ts.sb.postgresql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
public class Todo extends PanacheEntity {

    @Column(length = 40, unique = true)
    public String title;

    public boolean completed;

    public Todo() {
    }

    public Todo(String title, Boolean completed) {
        this.title = title;
    }

}