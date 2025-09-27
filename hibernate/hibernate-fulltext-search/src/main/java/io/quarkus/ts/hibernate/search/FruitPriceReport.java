package io.quarkus.ts.hibernate.search;

public record FruitPriceReport(PriceAggregation zeroToTen, PriceAggregation tenToTwenty, PriceAggregation twentyToInfinity) {
    public record PriceAggregation(int avg, int min, int max) {
        public PriceAggregation(double avg, double min, double max) {
            this((int) avg, (int) min, (int) max);
        }
    }
}
