package io.quarkus.ts.micrometer.prometheus;

import java.util.UUID;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.function.Supplier;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.micrometer.core.instrument.MeterRegistry;

@Path("/")
public class PrimeNumberResource {
    private static final int THREE = 3;
    private final LongAccumulator highestPrime = new LongAccumulator(Long::max, 0);
    private final MeterRegistry registry;
    private final String uniqueId = UUID.randomUUID().toString().substring(0, 5);

    PrimeNumberResource(MeterRegistry registry) {
        this.registry = registry;

        // Create a gauge that uses the highestPrimeNumberSoFar method
        // to obtain the highest observed prime number
        registry.gauge("prime.number.max." + uniqueId, this,
                PrimeNumberResource::highestObservedPrimeNumber);
    }

    @GET
    @Path("/uniqueId")
    @Produces(MediaType.TEXT_PLAIN)
    public String getUniqueId() {
        return uniqueId;
    }

    @GET
    @Path("/check/{number}")
    @Produces(MediaType.TEXT_PLAIN)
    public String checkIfPrime(@PathParam("number") long number) {
        if (number < 1) {
            return "Only natural numbers can be prime numbers.";
        }
        if (number == 1) {
            return "1 is not prime.";
        }
        if (number == 2) {
            return "2 is prime.";
        }
        if (number % 2 == 0) {
            return number + " is not prime, it is divisible by 2.";
        }

        Supplier<String> supplier = () -> {
            for (int i = THREE; i < Math.floor(Math.sqrt(number)) + 1; i = i + 2) {
                if (number % i == 0) {
                    return number + " is not prime, is divisible by " + i + ".";
                }
            }
            highestPrime.accumulate(number);
            return number + " is prime.";
        };

        return registry.timer("prime.number.test." + uniqueId).wrap(supplier).get();
    }

    /**
     * This method is called by the registered {@code prime.number.max} gauge.
     *
     * @return the highest observed prime value
     */
    long highestObservedPrimeNumber() {
        return highestPrime.get();
    }
}
