package io.quarkus.ts.langchain4j;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.langchain4j.RegisterAiService;
import io.smallrye.mutiny.Multi;

import dev.langchain4j.service.UserMessage;

@RegisterAiService(systemMessageProviderSupplier = ModelAwareSystemMessageProvider.class)
@ApplicationScoped
public interface Bot {
    // Using Multi enables streaming.
    Multi<String> chat(@UserMessage String question);
}
