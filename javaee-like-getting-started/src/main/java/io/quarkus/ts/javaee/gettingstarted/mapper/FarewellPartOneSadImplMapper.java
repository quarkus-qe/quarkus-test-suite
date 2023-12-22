package io.quarkus.ts.javaee.gettingstarted.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.JAKARTA;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import io.quarkus.ts.javaee.gettingstarted.data.FarewellPartOneSadImpl;
import io.quarkus.ts.javaee.gettingstarted.dto.FarewellPartOneDTO;

@Mapper(componentModel = JAKARTA)
public interface FarewellPartOneSadImplMapper {
    @Mapping(target = "data", constant = "sad")
    FarewellPartOneDTO sadMapping(FarewellPartOneSadImpl partOne);
}
