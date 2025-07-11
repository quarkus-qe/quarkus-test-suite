package io.quarkus.ts.jakarta.data.db;

import java.util.List;

import jakarta.data.repository.By;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Save;
import jakarta.data.repository.Update;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import org.hibernate.annotations.processing.Pattern;

public interface OtherFruitRepository {

    @Insert
    void insert(Fruit fruit);

    @Update
    void update(Fruit fruit);

    @Find
    Fruit findByName(@By("name") String name);

    @Find
    Fruit findByNamePattern(@Pattern String name);

    @Delete
    int deleteByName(String name);

    @Find
    List<Fruit> findAll();

    @Save
    void save(@Valid Fruit fruit);

    @Find
    Fruit findById(@Min(60) Long id);
}
