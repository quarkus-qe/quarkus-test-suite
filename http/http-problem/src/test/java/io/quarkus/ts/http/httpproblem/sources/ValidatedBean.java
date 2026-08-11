package io.quarkus.ts.http.httpproblem.sources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ValidatedBean {

    @NotBlank(message = "Name must not be blank")
    public String name;

    @Size(min = 2, max = 100, message = "Value must be between 2 and 100 characters")
    public String value;
}
