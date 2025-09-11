package io.quarkus.ts.jakarta.data.db;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Table(name = "fruit")
@Entity
public class Fruit {

    public Fruit() {
        this.name = null;
        this.id = null;
        this.dayOfWeek = DayOfWeek.FRIDAY;
    }

    public Fruit(String name, long id) {
        this.name = name;
        this.id = id;
        this.dayOfWeek = DayOfWeek.FRIDAY;
    }

    public Fruit(Long id, String name, DayOfWeek dayOfWeek) {
        this.id = id;
        this.name = name;
        this.dayOfWeek = dayOfWeek;
    }

    @Id
    Long id;

    @NotBlank
    String name;

    DayOfWeek dayOfWeek;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }
}
