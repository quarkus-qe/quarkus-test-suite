package io.quarkus.ts.qute;

import io.quarkus.qute.TemplateData;

@TemplateData(target = String.class)
@TemplateData //required for native
public record StringWrapper(String content) {
}
