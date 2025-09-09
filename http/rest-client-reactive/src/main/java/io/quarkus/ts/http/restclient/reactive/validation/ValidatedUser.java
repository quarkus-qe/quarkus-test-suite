package io.quarkus.ts.http.restclient.reactive.validation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;

public class ValidatedUser {

    @Size(min = 3, max = 20, groups = Default.class)
    @NotNull(groups = ConvertedGroup.class)
    public String username;

    public ValidatedUser(String value) {
        this.username = value;
    }

    public interface ConvertedGroup {
    }
}
