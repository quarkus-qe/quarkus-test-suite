package io.quarkus.ts.spring.data.primitivetypes.model;

import jakarta.persistence.MappedSuperclass;

//This is for regression test for https://github.com/quarkusio/quarkus/pull/13015
@MappedSuperclass
public class Mammal extends Animal {

}
