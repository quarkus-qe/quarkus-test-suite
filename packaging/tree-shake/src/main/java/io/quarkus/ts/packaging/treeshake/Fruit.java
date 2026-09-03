package io.quarkus.ts.packaging.treeshake;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Fruit {

    @Id
    @GeneratedValue
    public Long id;

    public String name;

    public Fruit() {
    }

    public Fruit(String name) {
        this.name = name;
    }
}
