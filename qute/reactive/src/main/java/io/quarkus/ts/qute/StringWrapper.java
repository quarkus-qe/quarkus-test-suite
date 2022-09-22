package io.quarkus.ts.qute;

import io.quarkus.qute.TemplateData;

@TemplateData(target = String.class)
@TemplateData //required for native
public class StringWrapper {
    public final String content;

    public StringWrapper(String content) {
        this.content = content;
    }
}
