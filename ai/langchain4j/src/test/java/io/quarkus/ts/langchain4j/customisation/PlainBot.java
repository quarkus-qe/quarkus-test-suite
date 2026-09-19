package io.quarkus.ts.langchain4j.customisation;

import jakarta.enterprise.context.ApplicationScoped;

import dev.langchain4j.service.UserMessage;

@ApplicationScoped
public interface PlainBot {
    // Using Multi enables streaming.
    String chat(@UserMessage String question);
}
