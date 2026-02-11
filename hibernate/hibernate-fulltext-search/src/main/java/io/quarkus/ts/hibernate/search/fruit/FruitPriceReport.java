package io.quarkus.ts.hibernate.search.fruit;

import io.quarkus.ts.hibernate.search.aggregation.PriceAggregation;

public record FruitPriceReport(PriceAggregation zeroToTen, PriceAggregation tenToTwenty,
        PriceAggregation twentyToInfinity) {

}
