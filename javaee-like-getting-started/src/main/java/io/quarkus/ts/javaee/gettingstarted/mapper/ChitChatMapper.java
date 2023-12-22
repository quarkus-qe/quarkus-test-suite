package io.quarkus.ts.javaee.gettingstarted.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.CDI;

import org.mapstruct.Mapper;

import io.quarkus.ts.javaee.gettingstarted.data.ChitChat;
import io.quarkus.ts.javaee.gettingstarted.dto.ChitChatDTO;

@Mapper(componentModel = CDI, uses = { ChitChatDtoFactory.class, ChitChatTopicMapper.class })
public abstract class ChitChatMapper {

    public abstract ChitChatDTO map(ChitChat chitChat);

}
