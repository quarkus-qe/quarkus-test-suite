package io.quarkus.ts.javaee.gettingstarted.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import io.quarkus.ts.javaee.gettingstarted.data.SeriousTalk;
import io.quarkus.ts.javaee.gettingstarted.dto.SeriousTalkDTO;

@Mapper
public interface SeriousTalkMapper {

    SeriousTalkMapper INSTANCE = Mappers.getMapper(SeriousTalkMapper.class);

    @Mapping(target = "start", source = "start.data")
    @Mapping(target = "middle", source = "middle.data")
    @Mapping(target = "end", source = "end.data")
    @Mapping(target = "postscript", defaultValue = "!")
    @Mapping(target = "ignored", ignore = true)
    SeriousTalkDTO map(SeriousTalk talk);

    @Mapping(target = "innerData", source = "nestedData.data")
    SeriousTalkDTO.DataDTO map(SeriousTalk.Data data);

    @BeforeMapping
    default void beforeMapping(SeriousTalk talk) {
        talk.setBefore("Ho Hey!");
    }

    @AfterMapping
    default void afterMapping(@MappingTarget SeriousTalkDTO.Builder builder) {
        builder.after("Enjoy yourself");
    }
}
