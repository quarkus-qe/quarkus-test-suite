package io.quarkus.ts.javaee.gettingstarted.data;

public record ChitChatTopic(SmileType type) {
    enum SmileType {
        AMERICAN,
        BRITISH
    }
}
