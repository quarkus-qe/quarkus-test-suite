package io.quarkus.ts.spring.data.primitivetypes;

import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.ts.spring.data.AbstractDbIT;
import io.quarkus.ts.spring.data.primitivetypes.model.Book;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusScenario
public class SpringDataCompositeEntityIT extends AbstractDbIT {

    @Test
    void testInterfaceBasedProjection() {
        app
                .given()
                .get("/book/projection/6")
                .then()
                .statusCode(SC_OK)
                .body("bid", is(6))
                .body("publicationYear", is(2015))
                .body("isbn", is(9789295055020L))
                .body("name", is("The Silk Roads"))
                .body("publisherAddress.id", is(1))
                .body("publisherAddress.zipCode", is("28080"));
    }

    @Test
    void testCustomFindPublicationYearPrimitiveInteger() {
        app.given().get("/book/customPublicationYearPrimitive/1").then()
                .statusCode(200)
                .body(is("2011"));
    }

    @Test
    void testCustomFindPublicationYearObjectInteger() {
        app.given().get("/book/customPublicationYearObject/1").then()
                .statusCode(200)
                .body(is("2011"));
    }

    @Test
    void testCustomFindPublicationIsbnPrimitiveLong() {
        app.given().get("/book/customPublicationIsbnPrimitive/2").then()
                .statusCode(200)
                .body(is("9789295055026"));
    }

    @Test
    void testCustomFindPublicationIsbnObjectLong() {
        app.given().get("/book/customPublicationIsbnObject/2").then()
                .statusCode(200)
                .body(is("9789295055026"));
    }

    //This is for regression test for https://github.com/quarkusio/quarkus/pull/13015
    @Test
    void testFindBooksByPublisherZipCode() {
        List<Book> books = retrieveBooksByZipcode();
        books.forEach(book -> assertThat(book.getPublisherAddress().getZipCode(), is("28080")));
    }

    //This is for regression test for https://github.com/quarkusio/quarkus/issues/13234
    @Test
    void testJpaFieldsMapping() {
        Book book = retrieveBooksByZipcode().stream().findFirst().get();

        // Post a new comment on an existing book
        Map<String, String> comment = Collections.singletonMap("Shakespeare", "Lorem Ipsum Lorem Ipsum");
        book.setComments(comment);
        book = updateBook(book);

        assertThat(book.getComments().size(), equalTo(1));
        assertThat(book.getComments(), equalTo(comment));

        // Update the previous comment
        Map<String, String> commentUpdated = Collections.singletonMap("Shakespeare", "Lorem Ipsum Lorem Ipsum novus");
        book.setComments(commentUpdated);
        book = updateBook(book);
        assertThat(book.getComments().size(), equalTo(1));
        assertThat(book.getComments(), equalTo(commentUpdated));
    }

    @Test
    void ensureFieldPageableContainsInfoAboutPage() {
        int size = 2;
        int page = 1;
        Response response = app.given()
                .accept("application/json")
                .queryParam("size", size)
                .queryParam("page", page)
                .when().get("/book/paged");
        Assertions.assertEquals(200, response.statusCode());
        LinkedHashMap<String, Object> pageable = response.body().jsonPath().get("pageable");
        Assertions.assertNotNull(pageable, "Field 'pageable' should not be null");
        Assertions.assertEquals(true, pageable.get("paged"),
                "The part of pageable response should contain paged equals to true");
        Assertions.assertEquals(size, pageable.get("pageSize"), "Element pageSize should be " + size);
        Assertions.assertEquals(page, pageable.get("pageNumber"), "Element pageNumber should be " + page);
    }

    private Book updateBook(Book book) {
        return app.given().contentType(ContentType.JSON).body(book).when().put("/book/" + book.getBid()).then()
                .statusCode(200).contentType(ContentType.JSON).extract().response().getBody().as(Book.class);
    }

    private List<Book> retrieveBooksByZipcode() {
        Response response = app.given().get("/book/publisher/zipcode/28080").then()
                .statusCode(200).contentType(ContentType.JSON).extract().response();

        List<Book> books = Arrays.asList(response.getBody().as(Book[].class));
        assertThat(books, is(not(empty())));
        return books;
    }
}
