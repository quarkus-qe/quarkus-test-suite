package io.quarkus.ts.jakarta.data;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.DatabaseService;
import io.quarkus.test.bootstrap.LookupService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.ts.jakarta.data.db.Address;
import io.quarkus.ts.jakarta.data.db.Author;
import io.quarkus.ts.jakarta.data.db.Book;
import io.quarkus.ts.jakarta.data.db.DayOfWeek;
import io.quarkus.ts.jakarta.data.db.Fruit;
import io.quarkus.ts.jakarta.data.db.FruitRepository;
import io.restassured.http.ContentType;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractJakartaDataIT {

    private static final int ID_RANGE_START = 55;

    protected static <T extends RestService> T createApp(Supplier<T> serviceCreator,
            DatabaseService<?> database, String dataSourceName) {
        var svc = serviceCreator.get();
        svc
                .withProperty("db-url", () -> database.getJdbcUrl())
                .withProperty("db-password", database::getPassword)
                .withProperty("db-username", database::getUser)
                .withProperty("quarkus.profile", dataSourceName);
        return svc;
    }

    protected static RestService createApp(DatabaseService<?> database, String dataSourceName) {
        return createApp(RestService::new, database, dataSourceName);
    }

    @LookupService
    static RestService app;

    @Order(1)
    @Test
    void testCrudRepositoryBuiltinInsert() {
        app.given()
                .contentType(ContentType.JSON)
                .body(new Fruit("apple", ID_RANGE_START))
                .post("/crud-repository/builtin/insert")
                .then().statusCode(200)
                .body("name", Matchers.is("apple"))
                .body("id", Matchers.notNullValue());
    }

    @Order(2)
    @Test
    void testCrudRepositoryBuiltinFindById() {
        app.given()
                .get("/crud-repository/builtin/find-by-id/" + ID_RANGE_START)
                .then().statusCode(200)
                .body("name", Matchers.is("apple"))
                .body("id", Matchers.is(ID_RANGE_START));
    }

    @Order(3)
    @Test
    void testCrudRepositoryEntityValidationAnnotation() {
        // Fruit field is annotated with @NotBlank and we expect invalid entity is denied
        app.given()
                .contentType(ContentType.JSON)
                .body(new Fruit("   ", ID_RANGE_START + 1))
                .post("/crud-repository/builtin/insert")
                // 'must not be blank' validation error is in the logs, but not disclosed in response
                .then().statusCode(500);
    }

    @Order(4)
    @Test
    void testCrudRepositoryBuiltinUpdate() {
        app.given()
                .contentType(ContentType.JSON)
                .body(new Fruit("pear", ID_RANGE_START))
                .put("/crud-repository/builtin/update")
                .then().statusCode(200)
                .body("name", Matchers.is("pear"))
                .body("id", Matchers.is(ID_RANGE_START));
    }

    @Order(5)
    @Test
    void testCrudRepositoryBuiltinInsertAll() {
        var requestBody = List.of(
                new Fruit("cherry", ID_RANGE_START + 1),
                new Fruit("apple", ID_RANGE_START + 2),
                new Fruit("raspberry", ID_RANGE_START + 3));
        var responseBody = app.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/crud-repository/builtin/insert-all")
                .then().statusCode(200)
                .extract().body().as(Fruit[].class);
        assertEquals(3, responseBody.length);
        assertFruitPresent(responseBody, "cherry");
        assertFruitPresent(responseBody, "apple");
        assertFruitPresent(responseBody, "raspberry");
    }

    @Order(6)
    @Test
    void testCrudRepositoryBuiltinFindAll() {
        var allFruits = app.given()
                .get("/crud-repository/builtin/find-all")
                .then().statusCode(200)
                .extract().body().as(Fruit[].class);
        assertEquals(4, allFruits.length);
        assertFruitPresent(allFruits, "cherry");
        assertFruitPresent(allFruits, "apple");
        assertFruitPresent(allFruits, "raspberry");
        assertFruitPresent(allFruits, "pear");
    }

    @Order(7)
    @Test
    void testCrudRepositoryBuiltinDeleteById() {
        app.given()
                .delete("/crud-repository/builtin/delete-by-id/" + (ID_RANGE_START + 1))
                .then().statusCode(204);
    }

    @Order(8)
    @Test
    void testCrudRepositoryQueryAnnotationJdqlCount() {
        app.given()
                .get("/crud-repository/query-annotation/jdql/count")
                .then().statusCode(200)
                .body(Matchers.is("3"));
    }

    @Order(9)
    @Test
    void testCrudRepositoryBuiltinFindAllWithPageAndOrder() {
        assumeNotSqlServerTest();
        // order is ascending and page size is 1 and page is 0 so we expect the first
        Fruit[] response = app.given()
                .queryParam("pageNumber", 1)
                .queryParam("pageSize", 1)
                .get("/crud-repository/builtin/find-all-with-page-and-order")
                .then().statusCode(200)
                .extract().body().as(Fruit[].class);
        assertEquals(1, response.length);
        assertEquals(ID_RANGE_START, response[0].getId());
        assertEquals("pear", response[0].getName());
        // now expect the last
        response = app.given()
                .queryParam("pageNumber", 3)
                .queryParam("pageSize", 1)
                .get("/crud-repository/builtin/find-all-with-page-and-order")
                .then().statusCode(200)
                .extract().body().as(Fruit[].class);
        assertEquals(1, response.length);
        assertEquals(ID_RANGE_START + 3, response[0].getId());
        assertEquals("raspberry", response[0].getName());
    }

    @Order(10)
    @Test
    void testCrudRepositoryBuiltinDelete() {
        app.given()
                .contentType(ContentType.JSON)
                .body(new Fruit("apple", ID_RANGE_START + 2))
                .delete("/crud-repository/builtin/delete")
                .then().statusCode(204);
    }

    @Order(11)
    @Test
    void testRepositoryQueryAnnotationWithRecord() {
        assumeNotSqlServerTest();
        var view = app.given().get("/repository/query-with-record")
                .then().statusCode(200)
                .body(Matchers.notNullValue())
                .extract().body().as(FruitRepository.View.class);
        // right now we expect to have 'raspberry' with id 58 and 'pear' with id 55
        assertEquals(2, view.count());
        // ordered by ids in descending order
        assertEquals("raspberry & pear", view.names());
        // ordered by ids in ascending order
        assertEquals("55,58", view.ids());
    }

    @Order(12)
    @Test
    void testRepositoryUpdateAnnotation() {
        app.given()
                .contentType(ContentType.JSON)
                .body(new Fruit("bilberry", ID_RANGE_START + 3))
                .put("/repository/update")
                .then().statusCode(204);
    }

    @Order(13)
    @Test
    void testRepositoryFindAnnotation() {
        app.given()
                .contentType(ContentType.JSON)
                .queryParam("name", "bilberry")
                .get("/repository/find-by-name")
                .then().statusCode(200)
                .body("name", Matchers.is("bilberry"))
                .body("id", Matchers.is(ID_RANGE_START + 3));
    }

    @Order(14)
    @Test
    void testRepositoryInsertAnnotation() {
        app.given()
                .contentType(ContentType.JSON)
                .body(new Fruit("blackberry", ID_RANGE_START + 4))
                .post("/repository/insert")
                .then().statusCode(204);
    }

    @Order(15)
    @Test
    void testRepositoryFindByPattern() {
        app.given()
                .contentType(ContentType.JSON)
                .queryParam("name", "ackberr")
                .get("/repository/find-by-pattern")
                .then().statusCode(200)
                .body("name", Matchers.is("blackberry"))
                .body("id", Matchers.is(ID_RANGE_START + 4));
    }

    @Order(16)
    @Test
    void testRepositoryDeleteAnnotationWithNameParameter() {
        app.given()
                .queryParam("name", "blackberry")
                .delete("/repository/delete-by-name")
                .then().statusCode(200)
                .body(Matchers.is("1"));
    }

    @Order(17)
    @Test
    void testRepositoryFindAnnotationToReturnAllRows() {
        var fruits = app.given().get("/repository/find-all")
                .then().statusCode(200)
                .extract().body().as(Fruit[].class);
        assertEquals(2, fruits.length);
        assertFruitPresent(fruits, "bilberry");
        assertFruitPresent(fruits, "pear");
    }

    @Order(18)
    @Test
    void testRepositorySaveAnnotation() {
        // blank name so expect validation failure
        app.given()
                .contentType(ContentType.JSON)
                .body(new Fruit("         ", ID_RANGE_START + 5))
                .patch("/repository/save")
                .then().statusCode(500);
        // name is fine so now the entity will be saved
        app.given()
                .contentType(ContentType.JSON)
                .body(new Fruit("plum", ID_RANGE_START + 5))
                .patch("/repository/save")
                .then().statusCode(204);
    }

    @Order(19)
    @Test
    void testRepositoryFindByIdWithValidation() {
        // validation is on repository method
        app.given()
                .queryParam("id", ID_RANGE_START + 5)
                .get("/repository/find-by-id")
                .then().statusCode(200)
                .body("id", Matchers.is(ID_RANGE_START + 5))
                .body("name", Matchers.is("plum"));
        // minimal value is 60 so we expect validation to fail for 55
        app.given()
                .queryParam("id", ID_RANGE_START)
                .get("/repository/find-by-id")
                .then().statusCode(500);
    }

    @Order(20)
    @Test
    void testRepositoryQueryWithJdqlFunctionCall() {
        // tests 'length()' JDQL function mentioned in Jakarta Data specs
        var nameLengths = app.given().get("/repository/query-with-jdql-function-call")
                .then().statusCode(200)
                .extract().body().as(FruitRepository.NameLengths[].class);
        assertEquals(3, nameLengths.length);
        boolean blumFound = false;
        boolean bilberryFound = false;
        boolean pearFound = false;
        for (FruitRepository.NameLengths nameLength : nameLengths) {
            switch (nameLength.name()) {
                case "bilberry":
                    bilberryFound = true;
                    break;
                case "pear":
                    pearFound = true;
                    break;
                case "plum":
                    blumFound = true;
                    break;
                default:
                    Assertions.fail("Unknown fruit " + nameLength.name());
            }
            assertEquals(nameLength.name().length(), nameLength.length());
        }
        assertTrue(blumFound, "Blum fruit not found");
        assertTrue(bilberryFound, "Bilberry fruit not found");
        assertTrue(pearFound, "Pear fruit not found");
    }

    @Order(21)
    @Test
    void testRepositoryQueryWithEnum() {
        var response = app.given().get("/repository/query-with-enum")
                .then().statusCode(200)
                .extract().body().as(Fruit[].class);
        assertEquals(3, response.length);
        assertFruitPresent(response, "bilberry");
        assertFruitPresent(response, "plum");
        assertFruitPresent(response, "pear");
        // make plum a Monday fruit
        app.given()
                .contentType(ContentType.JSON)
                .body(new Fruit((long) (ID_RANGE_START + 5), "plum", DayOfWeek.MONDAY))
                .patch("/repository/save")
                .then().statusCode(204);
        response = app.given().get("/repository/query-with-enum")
                .then().statusCode(200)
                .extract().body().as(Fruit[].class);
        assertEquals(2, response.length);
        assertFruitPresent(response, "bilberry");
        assertFruitPresent(response, "pear");
    }

    @Order(22)
    @Test
    void testCrudRepositoryExistsByEnum() {
        // current state:
        // pear - FRIDAY
        // bilberry - FRIDAY
        // plum - MONDAY
        app.given()
                .body(DayOfWeek.FRIDAY)
                .post("/crud-repository/exists-by-day-of-week")
                .then().statusCode(200)
                .body(Matchers.is("true"));
        app.given()
                .body(DayOfWeek.MONDAY)
                .post("/crud-repository/exists-by-day-of-week")
                .then().statusCode(200)
                .body(Matchers.is("true"));
        app.given()
                .body(DayOfWeek.TUESDAY)
                .post("/crud-repository/exists-by-day-of-week")
                .then().statusCode(200)
                .body(Matchers.is("false"));
    }

    @Order(23)
    @Test
    void testBasicRepositoryEntityAssociations() {
        var books1 = new ArrayList<Book>();
        var books2 = new ArrayList<Book>();
        var address1 = new Address("Klinova", "Ostrava", "70900");
        var address2 = new Address("Varsavska", "Ostrava", "70900");
        var author1 = new Author(ID_RANGE_START, "FirstName-1", "LastName-1", books1, address1);
        var author2 = new Author(ID_RANGE_START + 1, "FirstName-2", "LastName-2", books2, address2);
        books1.addAll(List.of(new Book(ID_RANGE_START, "Title-1", author1), new Book(ID_RANGE_START + 1, "Title-2", author1)));
        books2.addAll(
                List.of(new Book(ID_RANGE_START + 3, "Title-4", author2), new Book(ID_RANGE_START + 2, "Title-3", author2)));
        var authors = List.of(author1, author2);
        var response = app.given()
                .contentType(ContentType.JSON)
                .body(authors)
                .post("/basic-repository/save-all")
                .then().statusCode(200)
                .extract().body().as(Author[].class);
        assertEquals(2, response.length);
        Arrays.sort(response, Comparator.comparingLong(Author::getId));
        var author = response[0];
        assertEquals(ID_RANGE_START, author.getId());
        assertEquals("FirstName-1", author.getFirstName());
        assertEquals("LastName-1", author.getLastName());
        assertEquals(2, author.getBooks().size());
        var books = author.getBooks().stream().sorted(Comparator.comparingLong(Book::getId)).toList();
        var book1 = books.get(0);
        assertEquals(ID_RANGE_START, book1.getId());
        assertEquals("Title-1", book1.getTitle());
        var book2 = books.get(1);
        assertEquals(ID_RANGE_START + 1, book2.getId());
        assertEquals("Title-2", book2.getTitle());
        assertNotNull(author.getAddress());
        assertEquals("Klinova", author.getAddress().getStreet());
        assertEquals("70900", author.getAddress().getPostalCode());
        assertEquals("Ostrava", author.getAddress().getCity());
        author = response[1];
        assertEquals(ID_RANGE_START + 1, author.getId());
        assertEquals("FirstName-2", author.getFirstName());
        assertEquals("LastName-2", author.getLastName());
        assertEquals(2, author.getBooks().size());
        books = author.getBooks().stream().sorted(Comparator.comparingLong(Book::getId)).toList();
        book1 = books.get(0);
        assertEquals(ID_RANGE_START + 2, book1.getId());
        assertEquals("Title-3", book1.getTitle());
        book2 = books.get(1);
        assertEquals(ID_RANGE_START + 3, book2.getId());
        assertEquals("Title-4", book2.getTitle());
        assertNotNull(author.getAddress());
        assertEquals("Varsavska", author.getAddress().getStreet());
        assertEquals("70900", author.getAddress().getPostalCode());
        assertEquals("Ostrava", author.getAddress().getCity());
    }

    @Order(24)
    @Test
    void testBasicRepositoryFindAnnotationWithRangeAndLimit() {
        // order by title
        // limit 2
        // range - name prefix
        // we have 4 books with Title-1 to Title-4
        var books = app.given()
                .queryParam("limit", 2)
                .queryParam("name-prefix", "Title-")
                .get("/basic-repository/find-annotation-with-limit-and-order-by")
                .then().statusCode(200)
                .extract().body().as(Book[].class);
        assertEquals(2, books.length);
        assertEquals(ID_RANGE_START, books[0].getId());
        assertEquals("Title-1", books[0].getTitle());
        assertEquals(ID_RANGE_START + 1, books[1].getId());
        assertEquals("Title-2", books[1].getTitle());
        books = app.given()
                .queryParam("limit", 3)
                .queryParam("name-prefix", "Title-")
                .get("/basic-repository/find-annotation-with-limit-and-order-by")
                .then().statusCode(200)
                .extract().body().as(Book[].class);
        assertEquals(3, books.length);
        assertEquals(ID_RANGE_START, books[0].getId());
        assertEquals("Title-1", books[0].getTitle());
        assertEquals(ID_RANGE_START + 1, books[1].getId());
        assertEquals("Title-2", books[1].getTitle());
        assertEquals(ID_RANGE_START + 2, books[2].getId());
        assertEquals("Title-3", books[2].getTitle());
        books = app.given()
                .queryParam("limit", 3)
                .queryParam("name-prefix", "Title-4")
                .get("/basic-repository/find-annotation-with-limit-and-order-by")
                .then().statusCode(200)
                .extract().body().as(Book[].class);
        assertEquals(1, books.length);
        assertEquals(ID_RANGE_START + 3, books[0].getId());
        assertEquals("Title-4", books[0].getTitle());
    }

    @Disabled("https://github.com/quarkusio/quarkus/issues/49593")
    @Order(25)
    @Test
    void testBasicRepositoryDeleteUsingStatelessSessionDirectly() {
        app.given()
                .contentType(ContentType.JSON)
                .body(new Book(ID_RANGE_START + 1, "Title-2", new Author(ID_RANGE_START, null, null, null, null)))
                .delete("/basic-repository/author/" + ID_RANGE_START + "/delete-using-stateless-session-directly")
                .then().statusCode(204);
    }

    @Disabled("https://github.com/quarkusio/quarkus/issues/49593")
    @Order(26)
    @Test
    void testBasicRepositoryBuiltinFindById() {
        var books = app.given().get("/basic-repository/author/" + ID_RANGE_START + "/builtin/find-by-id")
                .then().statusCode(200)
                .body("id", Matchers.is(ID_RANGE_START))
                .body("firstName", Matchers.is("FirstName-1"))
                .body("lastName", Matchers.is("LastName-1"))
                .extract().body().as(Author.class).getBooks();
        assertEquals(1, books.size());
        assertEquals(ID_RANGE_START, books.get(0).getId());
        assertEquals("Title-1", books.get(0).getTitle());
    }

    @Order(27)
    @Test
    void testBasicRepositoryCdiInterceptors() {
        // user-defined interceptor
        app.given()
                .get("/repository/query-with-cdi-custom-interceptor")
                .then().statusCode(500);
        // security interceptors
        app.given()
                .auth().preemptive().basic("martin", "martin")
                .get("/repository/query-with-cdi-security-interceptor")
                .then().statusCode(200);
        // TODO: enable when https://github.com/quarkusio/quarkus/issues/48884 gets fixed
        //        app.given()
        //                .get("/repository/query-with-cdi-security-interceptor")
        //                .then().statusCode(401);
    }

    @Order(28)
    @Test
    void testCrudRepositoryResourceAccessorMethodForEntityManager() {
        app.given().delete("/repository/clean-up-fruit-table").then().statusCode(204);
    }

    private static void assertFruitPresent(Fruit[] responseBody, String fruitName) {
        assertTrue(Arrays.stream(responseBody).anyMatch(fruit -> fruit.getName().equals(fruitName)),
                () -> "Expected a fruit with name '" + fruitName + "', but got: " + Arrays.toString(responseBody));
    }

    private static void assumeNotSqlServerTest() {
        // SQL Server doesn't seem to support some SQL queries generated
        // from HQL; this is not an issue on our side and I don't seem that Hibernate must support all operations
        // for this DB
        boolean isSqlServerTest = app.getProperty("db-url").orElse("").contains("sqlserver");
        Assumptions.assumeFalse(isSqlServerTest);
    }
}
