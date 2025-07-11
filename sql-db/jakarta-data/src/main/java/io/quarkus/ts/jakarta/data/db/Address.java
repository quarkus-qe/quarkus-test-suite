package io.quarkus.ts.jakarta.data.db;

import jakarta.persistence.Embeddable;

@Embeddable
public class Address {

    public Address() {
    }

    public Address(String street, String city, String postalCode) {
        this.street = street;
        this.city = city;
        this.postalCode = postalCode;
    }

    String street;
    String city;
    String postalCode;

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getPostalCode() {
        return postalCode;
    }
}
