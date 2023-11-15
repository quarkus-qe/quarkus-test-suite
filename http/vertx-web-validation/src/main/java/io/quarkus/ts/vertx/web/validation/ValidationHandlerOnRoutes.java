package io.quarkus.ts.vertx.web.validation;

import static io.vertx.ext.web.validation.builder.Parameters.param;
import static io.vertx.json.schema.common.dsl.Schemas.arraySchema;
import static io.vertx.json.schema.common.dsl.Schemas.numberSchema;
import static io.vertx.json.schema.common.dsl.Schemas.objectSchema;
import static io.vertx.json.schema.common.dsl.Schemas.stringSchema;
import static io.vertx.json.schema.draft7.dsl.Keywords.maximum;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.validation.BadRequestException;
import io.vertx.ext.web.validation.BodyProcessorException;
import io.vertx.ext.web.validation.ParameterProcessorException;
import io.vertx.ext.web.validation.RequestParameters;
import io.vertx.ext.web.validation.RequestPredicate;
import io.vertx.ext.web.validation.RequestPredicateException;
import io.vertx.ext.web.validation.ValidationHandler;
import io.vertx.ext.web.validation.builder.Bodies;
import io.vertx.ext.web.validation.builder.Parameters;
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder;
import io.vertx.json.schema.SchemaParser;
import io.vertx.json.schema.SchemaRouter;
import io.vertx.json.schema.SchemaRouterOptions;
import io.vertx.json.schema.common.dsl.ObjectSchemaBuilder;

public class ValidationHandlerOnRoutes {
    //TODO when Quarkus use vert.x version 4.4.6 we can use SchemaRepository instead of SchemaParser with SchemaRouter
    //private SchemaRepository schemaRepository =SchemaRepository.create(new JsonSchemaOptions().setDraft(Draft.DRAFT7).setBaseUri(BASEURI));
    private SchemaParser schemaParser;
    private SchemaRouter schemaRouter;

    @Inject
    Vertx vertx;

    private static ShopResource shopResource = new ShopResource();

    private static final String ERROR_MESSAGE = "{\"error\": \"%s\"}";
    private static final String SHOPPINGLIST_NOT_FOUND = "Shopping list not found in the list or does not exist with that name or price";

    @PostConstruct
    void initialize() {
        schemaParser = createSchema();
    }

    private SchemaParser createSchema() {
        schemaRouter = SchemaRouter.create(vertx, new SchemaRouterOptions());
        schemaParser = SchemaParser.createDraft7SchemaParser(schemaRouter);
        return schemaParser;
    }

    public void validateHandlerShoppingList(@Observes Router router) {
        AtomicReference<String> queryAnswer = new AtomicReference<>();
        router.get("/filterList")
                .handler(ValidationHandlerBuilder
                        .create(schemaParser)
                        .queryParameter(param("shoppingListName", stringSchema()))
                        .queryParameter(param("shoppingListPrice", numberSchema().with(maximum(100)))).build())
                .handler(routingContext -> {
                    RequestParameters parameters = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
                    String shoppingListName = parameters.queryParameter("shoppingListName").getString();
                    Double totalPrice = parameters.queryParameter("shoppingListPrice").getDouble();

                    // Logic to list shoppingList based on shoppingListName and totalPrice
                    String shoppingListFound = fetchProductDetailsFromQuery(shoppingListName, totalPrice);
                    queryAnswer.set(shoppingListFound);

                    if (queryAnswer.get().equalsIgnoreCase(SHOPPINGLIST_NOT_FOUND)) {
                        routingContext.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
                    }

                    routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN).end(queryAnswer.get());
                }).failureHandler(routingContext -> {
                    // Error handling:
                    if (routingContext.failure() instanceof BadRequestException ||
                            routingContext.failure() instanceof ParameterProcessorException ||
                            routingContext.failure() instanceof BodyProcessorException ||
                            routingContext.failure() instanceof RequestPredicateException) {

                        String errorMessage = routingContext.failure().toString();
                        routingContext.response()
                                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                                .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .end(String.format(ERROR_MESSAGE, errorMessage));
                    } else {
                        routingContext.next();
                    }

                });
        // Create a ValidationHandlerBuilder with explodedParam and arraySchema to filter by array items
        ObjectSchemaBuilder bodySchemaBuilder = objectSchema()
                .property("shoppingListName", stringSchema());
        ValidationHandlerBuilder
                .create(schemaParser)
                .body(Bodies.json(bodySchemaBuilder));
        router.get("/filterByArrayItem")
                .handler(
                        ValidationHandlerBuilder
                                .create(schemaParser)
                                .queryParameter(Parameters.explodedParam("shoppingArray", arraySchema().items(stringSchema())))
                                .body(Bodies.json(bodySchemaBuilder))
                                .build())
                .handler(routingContext -> {
                    RequestParameters parameters = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
                    JsonArray myArray = parameters.queryParameter("shoppingArray").getJsonArray();
                    // Retrieve the list of all shoppingLists
                    List<ShoppingList> shoppingLists = fetchProductDetailsFromArrayQuery(myArray);

                    routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                            .end(Json.encodeToBuffer(shoppingLists));
                });
        // Let's allow to create a new item
        router.post("/createShoppingList").handler(
                ValidationHandlerBuilder
                        .create(schemaParser)
                        .predicate(RequestPredicate.BODY_REQUIRED)
                        .queryParameter(param("shoppingListName", stringSchema()))
                        .queryParameter(param("shoppingListPrice", numberSchema().with(maximum(100))))
                        .build())
                .handler(routingContext -> {
                    routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end("Shopping list created");
                });

    }

    public List<ShoppingList> fetchProductDetailsFromArrayQuery(JsonArray myArray) {
        return shopResource.get().stream()
                .filter(shoppingList -> myArray.contains(shoppingList.getName()))
                .collect(Collectors.toList());
    }

    public String fetchProductDetailsFromQuery(String name, Double price) {
        return shopResource.get().stream()
                .filter(product -> name.equalsIgnoreCase(product.getName()) && price.equals(product.getPrice()))
                .map(ShoppingList::toString)
                .findFirst()
                .orElse(SHOPPINGLIST_NOT_FOUND);
    }

}
