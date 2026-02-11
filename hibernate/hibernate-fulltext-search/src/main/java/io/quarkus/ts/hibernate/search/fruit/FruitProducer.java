package io.quarkus.ts.hibernate.search.fruit;

import java.util.List;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;

import io.quarkus.ts.hibernate.search.annotation.MyCustomIdGeneratorAnnotation;

@Entity
@Table(name = "fruit_producer")
public class FruitProducer {

    @Id
    @MyCustomIdGeneratorAnnotation
    private Integer id;

    @GenericField
    @Column(length = 40, unique = true)
    private String name;

    @JoinColumn(name = "fruit_id")
    @ManyToOne(targetEntity = Fruit.class)
    private Fruit fruit;

    public FruitProducer(Integer id, String name, Fruit fruit) {
        this.id = id;
        this.name = name;
        this.fruit = fruit;
    }

    public FruitProducer() {
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

    public Fruit getFruit() {
        // we need to break circular reference during serialization
        return new Fruit(fruit.getId(), fruit.getName(), fruit.getPrice(), List.of());
    }

    public void setFruit(Fruit fruit) {
        this.fruit = fruit;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        FruitProducer that = (FruitProducer) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "FruitProducer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
