package io.quarkus.ts.hibernate.reactive.database;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;

// Used in reproducer for QUARKUS-6792
@Entity
@NamedQuery(name = "Fruits.findAll", query = "SELECT f FROM Fruit f ORDER BY f.name")
public class Fruit {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(name = "something_name", nullable = false, updatable = false)
    @NotBlank(message = "Should not be blank!")
    @Length(max = 20, message = "Should be 20 chars max!")
    private String name;

    public Fruit() {
    }

    public Fruit(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Fruit{" + id + "," + name + '}';
    }
}
