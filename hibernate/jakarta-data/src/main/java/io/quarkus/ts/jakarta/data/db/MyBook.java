package io.quarkus.ts.jakarta.data.db;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class MyBook {
    @Id
    @GeneratedValue
    public Long id;

    public String title;

    public MyBook() {
    }

    public MyBook(String title) {
        this.title = title;
    }
}
