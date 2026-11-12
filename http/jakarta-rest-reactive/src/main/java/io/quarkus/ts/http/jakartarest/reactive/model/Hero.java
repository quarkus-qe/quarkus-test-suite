package io.quarkus.ts.http.jakartarest.reactive.model;

public record Hero(String name, Rank rank) implements Character {

    public enum Rank {
        S,
        A,
        B,
        F
    }
}
