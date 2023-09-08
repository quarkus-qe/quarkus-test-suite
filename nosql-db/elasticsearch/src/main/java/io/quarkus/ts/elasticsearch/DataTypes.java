package io.quarkus.ts.elasticsearch;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class DataTypes {
    public String id;
    public String name;
    public List<Fruit> fruits;
    public Date date;
    public float floatNum;
    public double doubleNum;

    @Override
    public String toString() {
        return "DataTypes{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", fruits=" + fruits +
                ", date=" + date +
                ", floatNum=" + floatNum +
                ", doubleNum=" + doubleNum +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DataTypes dataTypes = (DataTypes) o;
        return Float.compare(floatNum, dataTypes.floatNum) == 0 && Double.compare(doubleNum, dataTypes.doubleNum) == 0
                && Objects.equals(id, dataTypes.id) && Objects.equals(name, dataTypes.name)
                && Objects.equals(fruits, dataTypes.fruits) && Objects.equals(date, dataTypes.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, fruits, date, floatNum, doubleNum);
    }
}
