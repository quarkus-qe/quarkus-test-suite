package io.quarkus.ts.infinispan.client;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

public class Book {

    private String title;

    public Book() {
    }

    @ProtoFactory
    public Book(String title) {
        this.title = title;
    }

    @ProtoField(number = 1)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Book{title='" + title + "'}";
    }
}
