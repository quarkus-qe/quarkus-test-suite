package io.quarkus.ts.vertx.web.validation;

import java.util.ArrayList;
import java.util.UUID;

public class ShoppingList {

    public UUID id;

    public String name;

    public ArrayList<String> products;

    public double price;

    public ShoppingList(UUID id, String name, double price, ArrayList<String> products) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.products = products;
    }

    public ArrayList<String> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<String> products) {
        this.products = products;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return String.format(
                "Shopping list{id=%s, name=%s, products=%s, price=%s}",
                getId(),
                getName(),
                getProducts().toString(),
                getPrice());
    }
}
