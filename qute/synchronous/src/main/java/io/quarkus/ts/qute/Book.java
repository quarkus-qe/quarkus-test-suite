package io.quarkus.ts.qute;

import io.quarkus.qute.TemplateData;

@TemplateData
public record Book(String title, String author, String... characters) {
}
