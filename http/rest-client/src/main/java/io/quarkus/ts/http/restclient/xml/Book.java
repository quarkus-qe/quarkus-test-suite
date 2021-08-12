package io.quarkus.ts.http.restclient.xml;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Book {

    private String title;

    public Book() {
    }

    public Book(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
