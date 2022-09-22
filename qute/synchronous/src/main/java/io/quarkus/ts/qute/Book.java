package io.quarkus.ts.qute;

import io.quarkus.qute.TemplateData;

@TemplateData
public class Book {
    public final String title;
    public final String author;
    public final String[] characters;

    public Book(String title, String author, String... characters) {
        this.title = title;
        this.author = author;
        this.characters = characters;
    }
}
