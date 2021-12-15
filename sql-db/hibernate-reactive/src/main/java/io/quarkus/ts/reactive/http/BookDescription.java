package io.quarkus.ts.reactive.http;

import io.quarkus.hibernate.orm.panache.common.ProjectedFieldName;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class BookDescription {
    public final String title;

    public BookDescription(@ProjectedFieldName("title") String title) {
        this.title = title;
    }
}
