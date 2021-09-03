package io.quarkus.ts.jaxrs.reactive.json;

import com.fasterxml.jackson.annotation.JsonView;

public class User {

    @JsonView(Views.Private.class)
    public Integer id;

    @JsonView(Views.Public.class)
    public String name;
}
