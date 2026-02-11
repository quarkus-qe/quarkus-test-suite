package io.quarkus.ts.http.advanced.headers;

public class CustomHeaderResponse {

    private final String content;

    public CustomHeaderResponse(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
