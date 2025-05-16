package io.quarkus.ts.hibernate.reactive.validation;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// "https://github.com/quarkusio/quarkus/issues/47033"
public class PojoForIssue47033 {

    @NotNull
    @Size(min = 0)
    @Valid
    private List<NestedPojoForIssue47033> validatedList;

    @NotNull
    @Valid
    private NestedPojoForIssue47033 singleValidatedField;

}
