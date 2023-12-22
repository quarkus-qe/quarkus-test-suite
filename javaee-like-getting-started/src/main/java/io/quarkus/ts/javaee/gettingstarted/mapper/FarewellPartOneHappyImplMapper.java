package io.quarkus.ts.javaee.gettingstarted.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.JAKARTA;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import io.quarkus.ts.javaee.gettingstarted.data.FarewellPartOneHappyImpl;
import io.quarkus.ts.javaee.gettingstarted.dto.FarewellPartOneDTO;

@Mapper(componentModel = JAKARTA)
public interface FarewellPartOneHappyImplMapper {
    @Mapping(target = "data", source = "happy")
    FarewellPartOneDTO happyMapping(FarewellPartOneHappyImpl partOne);
}
