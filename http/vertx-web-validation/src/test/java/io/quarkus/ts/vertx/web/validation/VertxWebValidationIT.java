package io.quarkus.ts.vertx.web.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusScenario
public class VertxWebValidationIT {
    @QuarkusApplication
    static RestService restserviceapp = new RestService();

    private static final String SHOPPING_LIST_URL = "/shoppinglist";
    private static final String LIST_NAME1 = "name=ListName1, products=[Carrots, Water, Cheese, Beer], price=25.0";
    private static final String LIST_NAME2 = "name=ListName2, products=[Meat, Wine, Almonds, Potatoes, Cake], price=80.0";
    private static final String FILTER_BY_NAME_PRICE_URL_1 = "/filterList?shoppingListName=ListName1&shoppingListPrice=25";
    private static final String FILTER_BY_NAME_PRICE_URL_2 = "/filterList?shoppingListName=ListName2&shoppingListPrice=80";
    private static final String FILTER_BY_WRONG_NAME_PRICE_URL = "/filterList?shoppingListName=ListName35&shoppingListPrice=25";
    private static final String ONLY_FILTER_BY_NAME = "/filterList?shoppingListName=ListName1";
    private static final String PRICE_OUT_OF_RANGE = "/filterList?shoppingListName=ListName1&shoppingListPrice=125";
    private static final String FILTER_BY_LIST_NAMES = "/filterByArrayItem?shoppingArray=ListName1&shoppingArray=ListName2";

    private static final String ERROR_PARAMETER_MISSING = "ParameterProcessorException";

    @Test
    void checkShoppingListUrl() {
        Response response = restserviceapp.given()
                .get(SHOPPING_LIST_URL)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .response();

        assertThat(response.getBody().jsonPath().getString("name"), containsString("[ListName1, ListName2]"));
    }

    @Test
    void checkNamePriceParams() {
        Response response = restserviceapp
                .given()
                .get(FILTER_BY_NAME_PRICE_URL_1)
                .then()
                .statusCode(HttpStatus.SC_OK).extract().response();
        assertThat(response.asString(), containsString(LIST_NAME1));
        Response response2 = restserviceapp
                .given()
                .get(FILTER_BY_NAME_PRICE_URL_2)
                .then()
                .statusCode(HttpStatus.SC_OK).extract().response();
        assertThat(response2.asString(), containsString(LIST_NAME2));
    }

    @Test
    void checkWrongNamePriceParams() {
        Response response = restserviceapp
                .given()
                .get(FILTER_BY_WRONG_NAME_PRICE_URL)
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND).extract().response();
        assertThat(response.asString(),
                containsString("Shopping list not found in the list or does not exist with that name or price"));
    }

    @Test
    void checkParameterMissingError() {
        Response response = restserviceapp
                .given()
                .get(ONLY_FILTER_BY_NAME)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
        assertThat(response.asString(), containsString(ERROR_PARAMETER_MISSING));
        assertThat(response.asString(), containsString("Missing parameter shoppingListPrice in QUERY"));
    }

    @Test
    void checkPriceOutOfRangeError() {
        Response response = restserviceapp
                .given()
                .get(PRICE_OUT_OF_RANGE)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
        assertThat(response.asString(), containsString(ERROR_PARAMETER_MISSING));
        assertThat(response.asString(), containsString("value should be <= 100.0"));
    }

    @Test
    void checkFilterByArrayListName() {
        Response response = restserviceapp
                .given()
                .get(FILTER_BY_LIST_NAMES)
                .then()
                .statusCode(HttpStatus.SC_OK).extract().response();
        assertThat(response.getBody().jsonPath().getString("name"), containsString("[ListName1, ListName2]"));
        assertThat(response.getBody().jsonPath().getString("price"), equalTo("[25.0, 80.0]"));
    }

    @Test
    void createShoppingList() {
        restserviceapp.given()
                .contentType(ContentType.JSON)
                .body("{}")
                .queryParam("shoppingListName", "MyList3")
                .queryParam("shoppingListPrice", 50)
                .when()
                .post("/createShoppingList")
                .then()
                .statusCode(200)
                .body(equalTo("Shopping list created"));
    }

}
