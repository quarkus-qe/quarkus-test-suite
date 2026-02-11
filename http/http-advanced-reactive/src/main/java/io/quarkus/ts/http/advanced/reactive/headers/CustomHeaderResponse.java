package io.quarkus.ts.http.advanced.reactive.headers;

public class CustomHeaderResponse {
    private final String content;

    public CustomHeaderResponse(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
