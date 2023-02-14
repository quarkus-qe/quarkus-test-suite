package io.quarkus.ts.messaging.strimzi.kafka.reactive;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import org.jboss.resteasy.annotations.SseElementType;
import org.reactivestreams.Publisher;

import io.quarkus.ts.messaging.kafka.StockPrice;

@Path("/stock-price")
public class StockPriceEndpoint {

    @Inject
    @Channel("source-stock-price")
    @OnOverflow(value = OnOverflow.Strategy.DROP)
    Emitter<StockPrice> stockPriceEmitter;

    @Inject
    @Channel("price-stream")
    Publisher<String> stockPrices;

    @Inject
    @Channel("price-stream-batch")
    Publisher<List<String>> stockPricesBatch;

    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @SseElementType(MediaType.APPLICATION_JSON)
    public Publisher<String> stream() {
        return stockPrices;
    }

    @GET
    @Path("/stream-batch")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @SseElementType(MediaType.APPLICATION_JSON)
    public Publisher<List<String>> streamBatch() {
        return stockPricesBatch;
    }

    @POST
    public Response addStockPrice(StockPriceDto stockPrice) {
        stockPriceEmitter.send(stockPrice.toAvro());
        return Response.accepted().build();
    }
}
