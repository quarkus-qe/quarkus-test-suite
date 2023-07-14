package io.quarkus.ts.sqldb.multiplepus.model.fungus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import org.hibernate.annotations.TenantId;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
@Table(name = "fungus")
public class Fungus extends PanacheEntity {

    @TenantId
    @Column(length = 40)
    public String tenantId;

    @NotBlank(message = "Fungus name must be set!")
    public String name;

}
