package io.quarkus.ts.http.graphql;

public class Person {
    private final String name;
    private Person friend;

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
}
