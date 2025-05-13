package io.quarkus.ts.hibernate.reactive.validation;

import jakarta.validation.constraints.NotBlank;

// "https://github.com/quarkusio/quarkus/issues/47033"
public class NestedPojoForIssue47033 {

    @NotBlank
    private String name;

}
