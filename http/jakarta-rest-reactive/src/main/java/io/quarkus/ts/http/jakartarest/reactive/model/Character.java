package io.quarkus.ts.http.jakartarest.reactive.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Sealed hierarchy used to exercise Jackson polymorphic (de)serialization over REST. The
 * {@code type} discriminator selects the concrete record when a payload is deserialized.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Hero.class, name = "hero"),
        @JsonSubTypes.Type(value = Villain.class, name = "villain")
})
public sealed interface Character permits Hero, Villain {
    String name();
}
