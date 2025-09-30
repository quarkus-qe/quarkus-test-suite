package io.quarkus.ts.hibernate.search;

public record FruitPriceReport(PriceAggregation zeroToTen, PriceAggregation tenToTwenty,
        PriceAggregation twentyToInfinity) {

}
