package io.quarkus.ts.sb.reactive;

import javax.persistence.Column;
import javax.persistence.Entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;

@Entity
public class Todo extends PanacheEntity {

    @Column(length = 40, unique = true)
    public String title;

    public boolean completed;

    public Todo() {
    }

    public Todo(String title, Boolean completed) {
        this.title = title;
        this.completed = completed;
    }

}
