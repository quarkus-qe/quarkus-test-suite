package io.quarkus.ts.elasticsearch;

import java.util.Objects;
import java.util.UUID;

public class Fruit {
    public String id;
    public String name;
    public String color;

    public Fruit(String name, String color) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.color = color;
    }

    @Override
    public String toString() {
        return "Fruit{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", color='" + color + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Fruit fruit = (Fruit) o;
        return Objects.equals(id, fruit.id) && Objects.equals(name, fruit.name) && Objects.equals(color, fruit.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, color);
    }
}