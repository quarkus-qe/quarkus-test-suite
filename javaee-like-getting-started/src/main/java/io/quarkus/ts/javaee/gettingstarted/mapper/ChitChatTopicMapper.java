package io.quarkus.ts.javaee.gettingstarted.mapper;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.ts.javaee.gettingstarted.data.ChitChatTopic;

@ApplicationScoped
public class ChitChatTopicMapper {

    public String toTopicType(ChitChatTopic topic) {
        return "" + topic.type();
    }

}
