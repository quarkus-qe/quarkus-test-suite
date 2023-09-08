package io.quarkus.ts.vertx.sql.handlers;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import io.quarkus.ts.vertx.sql.handlers.spec.AirlineHandlerSpec;
import io.quarkus.ts.vertx.sql.handlers.spec.AirportHandlerSpec;
import io.quarkus.ts.vertx.sql.handlers.spec.BasketHandlerSpec;
import io.quarkus.ts.vertx.sql.handlers.spec.FlightsHandlerSpec;
import io.quarkus.ts.vertx.sql.handlers.spec.PricingRulesSpec;
import io.restassured.response.Response;

public abstract class CommonTestCases implements
        PricingRulesSpec, FlightsHandlerSpec, BasketHandlerSpec, AirportHandlerSpec,
        AirlineHandlerSpec {

    @Test
    public void pricingRuleScenario() {
        retrieveAllPricingRules();
    }

    @Test
    public void flightScenario() {
        retrieveAllFlights();
        retrieveFlightByOriginDestination();
        retrieveInfantFlightPrices();
        retrieveChildFlightPrices();
        retrieveAdultFlightPrices();
        retrieveFlightPrices();
        retrieveMultiplesFlightPrices();
        wrongFlightSearchFormat();
    }

    @Test
    public void basketScenario() {
        basketCheckout();
        wrongBasketFormatCheckout();
    }

    @Test
    public void airportScenario() {
        retrieveAllAirports();
    }

    @Test
    public void airlineScenario() {
        retrieveAllAirlines();
    }

    @Test
    void metrics() {
        Response response = given().get("q/metrics");
        assertEquals(200, response.statusCode());
        String body = response.body().asString();
        Set<String> metrics = Arrays.stream(body.split("\n"))
                .filter(line -> !line.startsWith("#"))
                .filter(line -> line.contains(getDBName()))
                .map(line -> line.split(" ")[0])
                .collect(Collectors.toSet());
        assertEquals(10, metrics.size(),
                String.format("There is an unexpected number of %s metrics in metrics: %s", getDBName(), body));
        assertTrue(metrics.contains(getMetricKey("processing_seconds_max")), "No processing_seconds_max in " + metrics);
        assertTrue(metrics.contains(getMetricKey("processing_seconds_count")), "No processing_seconds_count in " + metrics);
        assertTrue(metrics.contains(getMetricKey("processing_seconds_sum")), "No processing_seconds_sum in " + metrics);
        assertTrue(metrics.contains(getMetricKey("queue_size")), "No queue_size in " + metrics);
        assertTrue(metrics.contains(getMetricKey("reset_total")), "No reset_total in " + metrics);
        assertTrue(metrics.contains(getMetricKey("completed_total")), "No completed_total in " + metrics);
        assertTrue(metrics.contains(getMetricKey("queue_delay_seconds_count")), "No queue_delay_seconds_count in " + metrics);
        assertTrue(metrics.contains(getMetricKey("queue_delay_seconds_sum")), "No queue_delay_seconds_sum in " + metrics);
        assertTrue(metrics.contains(getMetricKey("queue_delay_seconds_max")), "No queue_delay_seconds_max in " + metrics);
        assertTrue(metrics.contains(getMetricKey("current")), "No current in " + metrics);
    }

    protected abstract CharSequence getDBName();

    protected abstract String getMetricKey(String metricName);
}
