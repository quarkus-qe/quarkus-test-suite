package io.quarkus.qe.hibernate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.fasterxml.jackson.databind.JsonNode;

import io.quarkus.qe.hibernate.hql.HQLConsoleClient;
import io.quarkus.test.bootstrap.LookupService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;

@Tag("QUARKUS-6243")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractHQLConsoleIT {

    private final int DEFAULT_PAGE_SIZE = 15;
    private static HQLConsoleClient hqlClient;
    private static final String PERSISTENCE_UNIT = "test-hql-pu";

    @LookupService
    static RestService app;

    @BeforeAll
    public static void beforeAll() {
        hqlClient = new HQLConsoleClient(app.getURI(Protocol.HTTP).toString(), PERSISTENCE_UNIT);
    }

    @AfterAll
    public static void tearDown() {
        if (hqlClient != null) {
            hqlClient.close();
        }
    }

    @Test
    @Order(1)
    public void testErrorForInvalidQuery() throws Exception {
        JsonNode dataSet = hqlClient.executeQuery("select invalid from None", 1, DEFAULT_PAGE_SIZE);

        assertNotNull(dataSet);
        assertErrorContains(dataSet, "Could not resolve root entity");
    }

    @Test
    @Order(2)
    public void testErrorForSyntaxError() throws Exception {
        String invalidQuery = "fram Address";
        JsonNode dataSet = hqlClient.executeQuery(invalidQuery, 1, DEFAULT_PAGE_SIZE);

        assertNotNull(dataSet);
        assertErrorContains(dataSet, "mismatched input 'fram'");
    }

    @Test
    @Order(3)
    public void testExecuteValidSimpleQuery() throws Exception {
        JsonNode dataSet = hqlClient.executeQuery("from Address where city = 'Prague'", 1, DEFAULT_PAGE_SIZE);

        assertNotNull(dataSet);
        assertEquals(1, dataSet.get("resultCount").intValue());
        assertEquals("Prague", dataSet.get("data").get(0).get("city").asText());
        assertEquals("Dejvicka", dataSet.get("data").get(0).get("street").asText());
    }

    @Test
    @Order(4)
    public void testGetAllAddressesAndVerifyCityNames() throws Exception {
        JsonNode dataSet = hqlClient.executeQuery("select a from Address a", 1, DEFAULT_PAGE_SIZE);

        assertNotNull(dataSet);
        assertEquals(3, dataSet.get("resultCount").intValue());

        JsonNode data = dataSet.get("data");
        assertEquals(3, data.size());

        List<String> cities = getMultipleResultsAsList(dataSet.get("data"), "city");
        assertTrue(cities.containsAll(List.of("Prague", "Brno", "Poznan")));
    }

    @Test
    @Order(5)
    public void testGetMultipleOrdersWithNestedValues() throws Exception {
        JsonNode dataSet = hqlClient.executeQuery(
                "select o from Orders o join fetch o.address join fetch o.items order by o.id asc",
                1,
                DEFAULT_PAGE_SIZE);

        assertNotNull(dataSet);
        assertEquals(9, dataSet.get("resultCount").intValue());

        JsonNode orders = dataSet.get("data");
        assertEquals(3, orders.size());

        // Check first order (Alice)
        JsonNode aliceOrder = orders.get(0);
        assertEquals("Alice", aliceOrder.get("customerName").asText());
        assertEquals("Prague", aliceOrder.get("address").get("city").asText());

        List<String> productNames = getMultipleResultsAsList(aliceOrder.get("items"), "productName");
        assertTrue(productNames.containsAll(List.of("Laptop", "Mouse", "Keyboard")));
    }

    @Test
    @Order(6)
    public void testHandlePagination() throws Exception {
        int pageSize = 4;
        int expectedTotalItems = 9;

        // Page 1 (first 4 items)
        JsonNode page1 = hqlClient.executeQuery("select i from OrderItem i order by i.id asc", 1, pageSize);
        assertNotNull(page1);
        assertEquals(expectedTotalItems, page1.get("resultCount").intValue());
        assertEquals(4, page1.get("data").size());
        assertEquals("Laptop", page1.get("data").get(0).get("productName").asText());
        assertEquals("Mouse", page1.get("data").get(1).get("productName").asText());
        assertEquals("Keyboard", page1.get("data").get(2).get("productName").asText());

        // Page 2 (next 4 items)
        JsonNode page2 = hqlClient.executeQuery("select i from OrderItem i order by i.id asc", 2, pageSize);
        assertNotNull(page2);
        assertEquals(expectedTotalItems, page2.get("resultCount").intValue());
        assertEquals(4, page2.get("data").size());
        assertEquals("Tablet", page2.get("data").get(1).get("productName").asText());

        // Page 3 (last item)
        JsonNode page3 = hqlClient.executeQuery("select i from OrderItem i order by i.id asc", 3, pageSize);
        assertNotNull(page3);
        assertEquals(expectedTotalItems, page3.get("resultCount").intValue());
        assertEquals(1, page3.get("data").size());
        assertEquals("Camera", page3.get("data").get(0).get("productName").asText());
    }

    @Test
    @Order(7)
    public void testInsertNewOrder() throws Exception {
        String insertHQL = "insert into Orders(id, customerName, address) " +
                "select 4, 'David', a from Address a where a.id = 2";
        JsonNode insertResult = hqlClient.executeQuery(insertHQL, 1, DEFAULT_PAGE_SIZE);

        assertNotNull(insertResult);
        assertMessageContains(insertResult, "Rows affected: 1");

        // Verify the new order exists
        JsonNode selectResult = hqlClient.executeQuery("select o from Orders o where o.customerName = 'David'",
                1, DEFAULT_PAGE_SIZE);
        assertEquals(1, selectResult.get("resultCount").intValue());
        assertEquals("David", selectResult.get("data").get(0).get("customerName").asText());
    }

    @Test
    @Order(8)
    public void testUpdateExistingAddress() throws Exception {
        String updateHQL = "update Address a set a.street = 'Veslarska' where a.id = 2";
        JsonNode updateResult = hqlClient.executeQuery(updateHQL, 1, DEFAULT_PAGE_SIZE);

        assertNotNull(updateResult);
        assertMessageContains(updateResult, "Rows affected: 1");

        JsonNode selectResult = hqlClient.executeQuery("select a from Address a where a.id = 2",
                1, DEFAULT_PAGE_SIZE);
        assertEquals("Veslarska", selectResult.get("data").get(0).get("street").asText());
    }

    @Test
    @Order(9)
    public void testDeletionViolatesFKConstraintException() throws Exception {
        String deleteHQL = "delete from Orders o where o.id = 2";
        JsonNode result = hqlClient.executeQuery(deleteHQL, 1, DEFAULT_PAGE_SIZE);

        assertNotNull(result);
        String error = result.has("error") ? result.get("error").asText() : "";
        String message = result.has("message") ? result.get("message").asText() : "";
        String expectedErrorText = "foreign key constraint";

        assertTrue(error.contains(expectedErrorText) || message.contains(expectedErrorText),
                "Expected error to contain '" + expectedErrorText);
    }

    @Test
    @Order(10)
    public void testDeleteOrderAndItemsByCustomerName() throws Exception {
        String customerName = "'Bob'";

        // First delete dependent order items, because of FK constraint on OrderItem -> Orders
        JsonNode deletedOrderItems = hqlClient.executeQuery(
                "delete from OrderItem i where i.orders.customerName = " + customerName,
                1, DEFAULT_PAGE_SIZE);
        assertNotNull(deletedOrderItems);
        assertMessageContains(deletedOrderItems, "Rows affected: 3");

        // Then delete customer's order(s)
        JsonNode deletedOrder = hqlClient.executeQuery("delete from Orders o where o.customerName = " + customerName,
                1, DEFAULT_PAGE_SIZE);
        assertNotNull(deletedOrder);
        assertMessageContains(deletedOrder, "Rows affected: 1");

        // Verify no orders remain for customer 'Bob'
        JsonNode returnedOrdersForBob = hqlClient.executeQuery("select o from Orders o where o.customerName = " + customerName,
                1, DEFAULT_PAGE_SIZE);
        assertEquals(0, returnedOrdersForBob.get("resultCount").intValue());
    }

    private void assertMessageContains(JsonNode node, String expectedText) {
        String message = node.has("message") ? node.get("message").asText() : "";
        assertTrue(message.contains(expectedText), "Expected message to contain '" + expectedText + "', but got: " + message);
    }

    private void assertErrorContains(JsonNode node, String expectedText) {
        String error = node.has("error") ? node.get("error").asText() : "";
        assertTrue(error.contains(expectedText), "Expected error to contain '" + expectedText + "', but got: " + error);
    }

    private List<String> getMultipleResultsAsList(JsonNode list, String fieldName) {
        List<String> values = new ArrayList<>();
        list.forEach(node -> values.add(node.get(fieldName).asText()));
        return values;
    }
}
