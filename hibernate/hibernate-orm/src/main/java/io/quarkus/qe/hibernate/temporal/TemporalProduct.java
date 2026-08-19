package io.quarkus.qe.hibernate.temporal;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

import org.hibernate.annotations.Temporal;

@Temporal(rowStart = "valid_from", rowEnd = "valid_to")
@Entity
public class TemporalProduct {

    @Id
    long id;

    @Version
    int version;

    String name;

    public TemporalProduct() {
    }

    public TemporalProduct(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
