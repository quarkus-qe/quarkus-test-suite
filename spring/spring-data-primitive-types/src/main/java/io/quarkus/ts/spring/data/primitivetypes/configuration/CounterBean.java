package io.quarkus.ts.spring.data.primitivetypes.configuration;

public class CounterBean {

    private final int amount;

    protected CounterBean(int count) {
        this.amount = count;
    }

    public int getAmount() {
        return amount;
    }
}
