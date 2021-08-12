package io.quarkus.ts.spring.properties;

import java.util.Map;

// Injecting maps is not supported. Reported in https://github.com/quarkusio/quarkus/issues/19366
// @ConfigurationProperties("maps")
public class MapWiringProperties {

    // Cover injection of maps of integers and strings;
    public Map<Integer, String> integers;

    // Cover injection of maps of string and classes
    public Map<String, Person> persons;

    public static class Person {
        public String name;
        public int age;

        @Override
        public String toString() {
            return String.format("person[%s:%s]", name, age);
        }
    }
}