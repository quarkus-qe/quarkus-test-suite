package io.quarkus.ts.sqldb.sqlapp.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record Fruit(
        Long id,
        @NotBlank @Size(max = 20) String name,
        @NotBlank @Size(max = 20) String description) {

    private static final Long DEFAULT_ID = -1L;

    public Fruit(String name, String description) {
        this(DEFAULT_ID, name, description);
    }

}
