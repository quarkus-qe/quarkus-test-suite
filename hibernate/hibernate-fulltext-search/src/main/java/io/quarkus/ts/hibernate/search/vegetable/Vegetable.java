package io.quarkus.ts.hibernate.search.vegetable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
@Table(name = "known_vegetables")
@XmlRootElement(name = "vegetable")
@Indexed
public class Vegetable extends PanacheEntity {

    public Vegetable() {
    }

    // this constructor is modified in DEV mode, don't delete it, or change it
    public Vegetable(String name, String description) {
        this.name = name;
    } // INSERT HERE

    @FullTextField(analyzer = "name")
    @Column(length = 40, unique = true)
    public String name;
}
