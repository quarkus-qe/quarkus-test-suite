package io.quarkus.ts.sqldb.multiplepus.model.vegetable;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
@Table(name = "vegetable")
public class Vegetable extends PanacheEntity {

    @NotBlank(message = "Vegetable name must be set!")
    public String name;

}
