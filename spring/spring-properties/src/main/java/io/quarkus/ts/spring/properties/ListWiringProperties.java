package io.quarkus.ts.spring.properties;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("lists")
public class ListWiringProperties {

    // Cover injection of lists of strings;
    public List<String> strings;

    // Inject of complex objects in lists is not supported: https://github.com/quarkusio/quarkus/issues/19365
    //    // Cover injection of classes
    //    public List<Person> persons;
    //
    //    public static class Person {
    //        public String name;
    //        public int age;
    //
    //        @Override
    //        public String toString() {
    //            return String.format("person[%s:%s]", name, age);
    //        }
    //    }
}