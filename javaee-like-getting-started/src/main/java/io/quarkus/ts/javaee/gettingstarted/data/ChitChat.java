package io.quarkus.ts.javaee.gettingstarted.data;

import static io.quarkus.ts.javaee.gettingstarted.data.ChitChatTopic.SmileType.AMERICAN;
import static io.quarkus.ts.javaee.gettingstarted.data.ChitChatTopic.SmileType.BRITISH;

public enum ChitChat {
    WEATHER(new ChitChatTopic(BRITISH)),
    TRUMP(new ChitChatTopic(AMERICAN));

    private final ChitChatTopic topic;

    ChitChat(ChitChatTopic topic) {
        this.topic = topic;
    }

    public ChitChatTopic getTopic() {
        return topic;
    }
}
