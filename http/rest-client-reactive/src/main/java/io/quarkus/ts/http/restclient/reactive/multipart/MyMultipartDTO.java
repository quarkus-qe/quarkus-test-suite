package io.quarkus.ts.http.restclient.reactive.multipart;

import java.util.List;

public class MyMultipartDTO {
    private List<Item> items;

    public MyMultipartDTO() {
    }

    public MyMultipartDTO(List<Item> items) {
        this.items = items;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}
