package io.quarkus.qe.model;

import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.vertx.core.json.Json;

@RegisterForReflection
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
}
