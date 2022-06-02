package io.quarkus.ts.spring.data.primitivetypes.model;

public interface BookProjection {

    Integer getBid();

    Integer getPublicationYear();

    Long getIsbn();

    Address getPublisherAddress();

    String getName();

}
