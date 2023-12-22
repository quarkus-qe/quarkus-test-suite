package io.quarkus.ts.javaee.gettingstarted.mapper;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.JAKARTA;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import io.quarkus.ts.javaee.gettingstarted.data.Farewell;
import io.quarkus.ts.javaee.gettingstarted.data.FarewellPartOne;
import io.quarkus.ts.javaee.gettingstarted.data.FarewellPartOneDefaultMethod;
import io.quarkus.ts.javaee.gettingstarted.dto.FarewellDTO;
import io.quarkus.ts.javaee.gettingstarted.dto.FarewellPartOneDTO;

@DecoratedWith(FarewellMapperDecorator.class)
@Mapper(componentModel = JAKARTA, injectionStrategy = CONSTRUCTOR)
public interface FarewellMapper {

    @Mapping(target = "partOne", source = "partOne")
    @Mapping(target = "partTwo", expression = "java(Integer.toString(farewell.partTwo().second()))")
    FarewellDTO map(Farewell farewell);

    default FarewellPartOneDTO partOneToString(FarewellPartOne partOne) {
        return new FarewellPartOneDTO("original");
    }

    default FarewellPartOneDTO defaultMethod(FarewellPartOneDefaultMethod partOne) {
        return new FarewellPartOneDTO("default");
    }

}
