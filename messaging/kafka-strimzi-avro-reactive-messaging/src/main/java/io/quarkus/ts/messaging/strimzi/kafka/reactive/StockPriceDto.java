package io.quarkus.ts.messaging.strimzi.kafka.reactive;

import io.quarkus.ts.messaging.kafka.StockPrice;
import io.quarkus.ts.messaging.kafka.status;

public class StockPriceDto {
    private String id;
    private double value;

    public StockPrice toAvro() {
        return StockPrice.newBuilder()
                .setId(id)
                .setPrice(value)
                .setStatus(status.PENDING)
                .build();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
