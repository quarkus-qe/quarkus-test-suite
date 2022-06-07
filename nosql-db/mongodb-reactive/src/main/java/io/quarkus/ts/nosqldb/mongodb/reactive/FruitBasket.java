package io.quarkus.ts.nosqldb.mongodb.reactive;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bson.Document;

public class FruitBasket {
    private String name;
    private List<Fruit> items;
    private String id;

    public FruitBasket() {
    }

    public FruitBasket(String name, List<Fruit> items) {
        this.name = name;
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Fruit> getItems() {
        return items;
    }

    public void setItems(List<Fruit> items) {
        this.items = items;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FruitBasket that = (FruitBasket) o;
        return Objects.equals(name, that.name) && Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, items);
    }

    public static FruitBasket fromDocument(Document document) {
        final List<Document> items = document.getList("items", Document.class);
        final List<Fruit> fruits = items == null ? null : items.stream().map(Fruit::fromDocument).collect(Collectors.toList());
        return new FruitBasket(document.getString("name"), fruits);
    }

    public Document toDocument() {
        List<Document> items = this.getItems() == null ? null
                : this.getItems().stream().map(Fruit::toDocument).collect(Collectors.toList());
        return new Document()
                .append("name", this.getName())
                .append("items", items);
    }
}
