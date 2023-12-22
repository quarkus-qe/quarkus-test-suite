package io.quarkus.ts.javaee.gettingstarted.mapper;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import io.quarkus.ts.javaee.gettingstarted.data.Farewell;
import io.quarkus.ts.javaee.gettingstarted.data.FarewellPartOne;
import io.quarkus.ts.javaee.gettingstarted.data.FarewellPartOneDefaultMethod;
import io.quarkus.ts.javaee.gettingstarted.data.FarewellPartOneHappyImpl;
import io.quarkus.ts.javaee.gettingstarted.data.FarewellPartOneSadImpl;
import io.quarkus.ts.javaee.gettingstarted.dto.FarewellDTO;
import io.quarkus.ts.javaee.gettingstarted.dto.FarewellPartOneDTO;

public abstract class FarewellMapperDecorator implements FarewellMapper {

    @Named("io.quarkus.ts.javaee.gettingstarted.mapper.FarewellMapperImpl_")
    @Inject
    FarewellMapper originalMapper;

    @Inject
    FarewellPartOneHappyImplMapper happyImplMapper;

    @Inject
    FarewellPartOneSadImplMapper sadImplMapper;

    @Override
    public FarewellDTO map(Farewell farewell) {
        var originalResult = originalMapper.map(farewell);
        return new FarewellDTO(partOneToString(farewell.partOne()), originalResult.partTwo());
    }

    @Override
    public FarewellPartOneDTO partOneToString(FarewellPartOne partOne) {
        String joined = String.join("_", originalMapper.partOneToString(partOne).getData(),
                instanceSpecificMapping(partOne).getData());
        return new FarewellPartOneDTO(joined);
    }

    private FarewellPartOneDTO instanceSpecificMapping(FarewellPartOne partOne) {
        if (partOne instanceof FarewellPartOneHappyImpl happyImpl) {
            return happyImplMapper.happyMapping(happyImpl);
        } else if (partOne instanceof FarewellPartOneSadImpl sadImpl) {
            return sadImplMapper.sadMapping(sadImpl);
        } else if (partOne instanceof FarewellPartOneDefaultMethod defaultMethod) {
            return defaultMethod(defaultMethod);
        } else {
            throw new IllegalStateException("FarewellPartOne is sealed interface that does not support other implementors");
        }
    }
}
