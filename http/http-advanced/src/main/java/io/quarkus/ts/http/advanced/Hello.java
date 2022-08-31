package io.quarkus.ts.http.advanced;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.smallrye.common.constraint.NotNull;

public class Hello {

    @NotNull
    @Size(min = 1, max = 4)
    @Pattern(regexp = "^[A-Za-z]+$")
    private final String content;

    public Hello(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
