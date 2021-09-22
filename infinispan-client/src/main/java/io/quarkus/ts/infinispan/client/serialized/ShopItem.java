package io.quarkus.ts.infinispan.client.serialized;

import java.util.Objects;

import org.infinispan.protostream.annotations.ProtoEnumValue;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

public class ShopItem {

    private String title;
    private int price;
    private Type type;

    public ShopItem() {
    }

    @ProtoFactory
    public ShopItem(String title, int price, Type type) {
        this.title = title;
        this.price = price;
        this.type = type;
    }

    @ProtoField(number = 1)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @ProtoField(number = 2, defaultValue = "-1")
    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @ProtoField(number = 3)
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        @ProtoEnumValue(number = 1, name = "ELEC")
        ELECTRONIC,
        @ProtoEnumValue(number = 2, name = "MECH")
        MECHANICAL
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ShopItem shopItem = (ShopItem) o;
        return price == shopItem.price &&
                title.equals(shopItem.title) &&
                type == shopItem.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, price, type);
    }
}
