package io.quarkus.ts.sqldb.sqlapp.model;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.LockModeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.NotFoundException;

import io.quarkus.ts.sqldb.sqlapp.storage.FruitEntity;

public record Fruit(
        Long id,
        @NotBlank @Size(max = 20) String name,
        @NotBlank @Size(max = 20) String description) implements FruitMarshaller {

    private final static Long DEFAULT_ID = -1L;

    public Fruit(String name, String description) {
        this(DEFAULT_ID, name, description);
    }

    public Fruit(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public static List<Fruit> getAll() {
        return FruitEntity.<FruitEntity> find("#Fruits.findAllCustom")
                .stream()
                .map(FruitMarshaller::fromEntity)
                .collect(Collectors.toList());
    }

    public static Fruit getLongestDescription() {
        return FruitMarshaller.fromEntity(FruitEntity.getLongestDescription());
    }

    public Fruit save() {
        var fruitEntity = FruitMarshaller.toEntity(this);
        FruitEntity.persist(fruitEntity);
        return FruitMarshaller.fromEntity(fruitEntity);
    }

    public Fruit update(Long id) {
        FruitEntity fruit = FruitEntity.findById(id);
        if (fruit == null) {
            throw new NotFoundException();
        }

        fruit.name = this.name;
        fruit.description = this.description;
        FruitEntity.findById(id, LockModeType.PESSIMISTIC_WRITE).persist();

        return FruitMarshaller.fromEntity(fruit);
    }

    public static boolean deleteById(Long id) {
        return FruitEntity.deleteById(id);
    }

}
