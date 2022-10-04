# Table of contents
1. [Quarkus Spring Data JPA Extension](#quarkus-spring-data-jpa-extension)
1. [Quarkus Spring DI](#quarkus-spring-di)
1. [Quarkus - Spring Data REST](#quarkus---spring-data-rest)

# Quarkus Spring Data JPA Extension

## Issues references:
[Fix spring-data-jpa field lookup with layered @MappedSuperclasses](https://issues.redhat.com/browse/QUARKUS-532)
[Make embedded fields with camel-case work in Spring Data JPA repositories](https://issues.redhat.com/browse/QUARKUS-525)
[Spring's @Scope#scopeName is now taken into account](https://issues.redhat.com/browse/QUARKUS-547)

## Fix spring-data-jpa field lookup with layered @MappedSuperclasses

We have added a "super" structure over the existing Cat entity. 
So currently,  Cats extends Mammal and mammal are animals.

## Make embedded fields with camel-case work in Spring Data JPA repositories

We've created a query over an embedded camelCase field.

## Spring's @Scope#scopeName is now taken into account

We've created an HTTP Request filter in order to add headers to the response:

- x-session: Keep the same value per HTTP session
- x-count: count the number of requests
- x-request: increase his value per HTTP request
- x-instance: represents the instance ID. Must be a unique per pod/instance.

# Quarkus Spring DI

## Issues references
[Document: Using the Quarkus Extension for Spring DI API](https://issues.redhat.com/browse/QUARKUS-184)

## Scope of testing
Beans defined using Spring DI annotations:
- Verify presence in CDI context.
- Verify injected transitive dependencies.
- Verify multiple ways of retrieving beans from the context.

# Quarkus - Spring Data REST

## Scope of the testing

Used addition features and functionality:

- Hibernate Validator
- One-to-Many and Many-to-One relationship
- DevServices

Used quarkus-spring-data-rest repositories:

- CrudRepository
- PagingAndSortingRepository
- JpaRepository

Test for the CrudRepository and JpaRepository:

- Verify all the CRUD methods are available for end user
- Verify invalid input data
- Verify entity validation

Additional test for the PagingAndSortingRepository:

- Redefine default path via *path* attribute
- Define collection's root name via *collectionResourceRel* attribute
- Export only certain CRUD methods and restrict the use of others for the end-user
- Verify pagination and sorting are working correct

## Requirements

To compile and run this demo you will need:

- JDK 1.8+
- GraalVM

In addition, you will need either a PostgreSQL database, or Docker to run one. Database for tests handled automatically using
Testcontainers and enriched via `import.sql` file.

To run PostgreSQL database in Docker run:
> docker run --ulimit memlock=-1:-1 -it --rm=true --memory-swappiness=0 --name quarkus_test -e POSTGRES_USER=quarkus_test -e POSTGRES_PASSWORD=quarkus_test -e POSTGRES_DB=quarkus_test -p 5432:5432 postgres:11.5

Connection properties for the Agroal datasource are defined in the standard Quarkus configuration file,
`src/main/resources/application.properties`.
