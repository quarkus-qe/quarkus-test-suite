package io.quarkus.ts.http.graphql;

import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;

public class Person {
    private final String name;

    private Person friend;

    @AdaptToScalar(Scalar.String.class)
    private Person idol;

    public Person(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setFriend(Person friend) {
        this.friend = friend;
    }

    public Person getFriend() {
        return friend;
    }

    public Person getIdol() {
        return idol;
    }

    public void setIdol(Person idol) {
        this.idol = idol;
    }

    @Override
    public String toString() {
        return name;
    }
}
