package io.quarkus.ts.hibernate.reactive.database;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class XmlValidatedCustomer {

    @Id
    @GeneratedValue
    public Long id;

    public String name;
    public String email;

    public XmlValidatedCustomer() {
    }

    public XmlValidatedCustomer(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
