package io.quarkus.qe.model;

import java.util.ArrayList;
import java.util.List;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.Json;

public class Record {

    protected static final String QUALIFIED_ID = "id";
    private Long id;

    public Record() {
        // default constructor.
    }

    public Record(long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String toJsonStringify() {
        return Json.encode(this);
    }

    public static String toJsonStringify(List<? extends Record> records) {
        return Json.encode(records);
    }

    public static <T> Uni<List<T>> toList(Multi<T> records) {
        return records.collect().in(ArrayList::new, List::add);
    }
}
