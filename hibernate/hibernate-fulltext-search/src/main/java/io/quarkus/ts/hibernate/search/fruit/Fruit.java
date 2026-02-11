package io.quarkus.ts.hibernate.search.fruit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;

import io.quarkus.ts.hibernate.search.annotation.MyCustomIdGeneratorAnnotation;

@Entity
@Table(name = "known_fruits")
@NamedQuery(name = "Fruits.findAll", query = "SELECT f FROM Fruit f ORDER BY f.name")
@NamedQuery(name = "Fruits.findByName", query = "SELECT f FROM Fruit f WHERE f.name=:name")
@XmlRootElement(name = "fruit")
@Indexed
public class Fruit {

    @Id
    @MyCustomIdGeneratorAnnotation
    private Integer id;

    @FullTextField(analyzer = "name")
    @KeywordField(name = "fruitName_sort", sortable = Sortable.YES, normalizer = "sort")
    @Column(length = 40, unique = true)
    private String name;

    @GenericField(aggregable = Aggregable.YES)
    @Column
    private Double price;

    @IndexedEmbedded
    @OneToMany(mappedBy = "fruit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<FruitProducer> producers = new ArrayList<>();

    public Fruit() {
    }

    public Fruit(String name) {
        this.name = name;
    }

    public Fruit(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Fruit(Integer id, String name, Double price, List<FruitProducer> producers) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.producers = producers;
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public List<FruitProducer> getProducers() {
        return producers;
    }

    public void setProducers(List<FruitProducer> producers) {
        this.producers = producers;
    }

    public void addProducer(FruitProducer producer) {
        this.producers.add(producer);
        producer.setFruit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        Fruit fruit = (Fruit) o;
        return Objects.equals(id, fruit.id) && Objects.equals(name, fruit.name) && Objects.equals(price, fruit.price)
                && Objects.equals(producers, fruit.producers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, price, producers);
    }

    @Override
    public String toString() {
        return "Fruit{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", producers=" + producers +
                '}';
    }
}
