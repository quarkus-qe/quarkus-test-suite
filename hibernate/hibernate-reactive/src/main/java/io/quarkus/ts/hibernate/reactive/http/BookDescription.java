package io.quarkus.ts.hibernate.reactive.http;

import io.quarkus.hibernate.orm.panache.common.ProjectedFieldName;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record BookDescription(String title) {
    public BookDescription(@ProjectedFieldName("title") String title) {
        this.title = title;
    }
}
