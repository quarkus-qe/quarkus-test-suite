package io.quarkus.ts.sqldb.multiplepus.model.fruit;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
@Table(name = "fruit")
public class Fruit extends PanacheEntity {

    @NotBlank(message = "Fruit name must be set!")
    public String name;

}
