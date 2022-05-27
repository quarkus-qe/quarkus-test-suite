package io.quarkus.ts.http.advanced.reactive;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class MultipartFormDataDTO {

    private final String text;
    private final String file;

    public MultipartFormDataDTO(String text, String file) {
        this.text = text;
        this.file = file;
    }

    public String getText() {
        return text;
    }

    public String getFile() {
        return file;
    }
}
