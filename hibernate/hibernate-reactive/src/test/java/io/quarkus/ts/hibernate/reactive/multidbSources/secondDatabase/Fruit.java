package io.quarkus.ts.hibernate.reactive.multidbSources.secondDatabase;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "fruits")
/*
 * This class is for testing multi-database (named datasources and persistence units) tests.
 * For that, it is required that this entity is in separate package and is not linked to another entity in different PU.
 */
public class Fruit {
    private static final int MAX_NAME_LENGTH = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    @NotNull
    @Size(max = MAX_NAME_LENGTH)
    private String name;

    public Fruit() {
    }

    public Fruit(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public @NotNull @Size(max = MAX_NAME_LENGTH) String getName() {
        return name;
    }

    public void setName(@NotNull @Size(max = MAX_NAME_LENGTH) String name) {
        this.name = name;
    }
}
