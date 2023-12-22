package io.quarkus.ts.javaee.gettingstarted.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.JAKARTA_CDI;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import io.quarkus.ts.javaee.gettingstarted.data.Greeting;
import io.quarkus.ts.javaee.gettingstarted.dto.GreetingDTO;

@Mapper(componentModel = JAKARTA_CDI)
public interface GreetingMapper {

    @Mapping(target = "hello", source = "hello")
    @Mapping(target = "from", source = "from.data")
    @Mapping(target = "to", source = "to.data")
    GreetingDTO map(Greeting greeting);

}
