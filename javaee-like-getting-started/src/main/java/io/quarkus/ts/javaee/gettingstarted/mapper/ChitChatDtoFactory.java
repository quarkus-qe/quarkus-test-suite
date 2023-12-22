package io.quarkus.ts.javaee.gettingstarted.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.CDI;

import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;

import io.quarkus.ts.javaee.gettingstarted.data.ChitChat;
import io.quarkus.ts.javaee.gettingstarted.dto.ChitChatDTO;

@Mapper(componentModel = CDI)
public class ChitChatDtoFactory {

    @ObjectFactory
    public ChitChatDTO createChitChatImpl(ChitChat chitChat) {
        // creates impl. as ChitChatDTO is an interface
        // we can happily ignore the 'chitChat' var because point here is to create object that is used for mapping
        return new ChitChatDTO() {

            private String topic;

            @Override
            public String getTopic() {
                return topic;
            }

            @Override
            public void setTopic(String topic) {
                this.topic = topic;
            }
        };
    }

}
