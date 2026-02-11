package io.quarkus.ts.hibernate.search.aggregation;

public record PriceAggregation(int avg, int min, int max) {
    public PriceAggregation(double avg, double min, double max) {
        this((int) avg, (int) min, (int) max);
    }
}
