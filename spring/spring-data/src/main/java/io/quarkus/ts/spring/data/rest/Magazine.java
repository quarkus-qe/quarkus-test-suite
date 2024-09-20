package io.quarkus.ts.spring.data.rest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Magazine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name may not be blank")
    private String name;

    @Column(name = "issued_in")
    @Min(1800)
    private Long issuedIn;

    public Magazine() {
    }

    public Magazine(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Magazine withName(String name) {
        this.name = name;
        return this;
    }

    public Long getIssuedIn() {
        return issuedIn;
    }

    public void setIssuedIn(Long issuedIn) {
        this.issuedIn = issuedIn;
    }
}
