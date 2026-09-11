package io.quarkus.ts.langchain4j.customisation;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.langchain4j.RegisterAiService;

import dev.langchain4j.service.UserMessage;

@ApplicationScoped
@RegisterAiService(modelName = "custom")
public interface NamedBot {
    // Using Multi enables streaming.
    String chat(@UserMessage String question);
}
