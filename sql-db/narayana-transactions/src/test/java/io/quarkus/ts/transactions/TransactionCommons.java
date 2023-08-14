package io.quarkus.ts.transactions;

import static io.quarkus.test.utils.AwaitilityUtils.untilAsserted;
import static io.quarkus.ts.transactions.recovery.driver.CrashingXAResource.RECOVERY_SUBPATH;
import static io.quarkus.ts.transactions.recovery.driver.CrashingXAResource.TRANSACTION_LOGS_PATH;
import static io.restassured.RestAssured.given;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.JaegerService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.JaegerContainer;
import io.quarkus.ts.transactions.recovery.TransactionExecutor;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // we keep order to ensure JDBC traces are ready
public abstract class TransactionCommons {

    private static final String ENABLE_TRANSACTION_RECOVERY = "quarkus.transaction-manager.enable-recovery";
    static final String ACCOUNT_NUMBER_MIGUEL = "SK0389852379529966291984";
    static final String ACCOUNT_NUMBER_GARCILASO = "FR9317569000409377431694J37";
    static final String ACCOUNT_NUMBER_LUIS = "ES8521006742088984966816";
    static final String ACCOUNT_NUMBER_LOPE = "CZ9250512252717368964232";
    static final String ACCOUNT_NUMBER_FRANCISCO = "ES8521006742088984966817";

    static final String ACCOUNT_NUMBER_EDUARDO = "ES8521006742088984966899";
    static final int ASSERT_SERVICE_TIMEOUT_MINUTES = 1;

    @JaegerContainer(expectedLog = "\"Health Check state change\",\"status\":\"ready\"")
    static final JaegerService jaeger = new JaegerService();

    protected abstract RestService getApp();

    /**
     * As it would be too expensive to try recovery for each an every type of transaction
     * (and probably it makes little sense for recovery happens on lower layer)
     * we assign each executor to a database so that we test all transaction executors.
     */
    protected abstract TransactionExecutor getTransactionExecutorUsedForRecovery();

    @Order(1)
    @Tag("QUARKUS-2492")
    @Test
    public void verifyNarayanaProgrammaticApproachTransaction() {
        TransferDTO transferDTO = new TransferDTO();
        transferDTO.setAccountFrom(ACCOUNT_NUMBER_MIGUEL);
        transferDTO.setAccountTo(ACCOUNT_NUMBER_LOPE);
        transferDTO.setAmount(100);

        given()
                .contentType(ContentType.JSON)
                .body(transferDTO).post("/transfer/transaction")
                .then().statusCode(HttpStatus.SC_CREATED);

        AccountEntity miguelAccount = getAccount(ACCOUNT_NUMBER_MIGUEL);
        Assertions.assertEquals(0, miguelAccount.getAmount(), "Unexpected amount on source account.");

        AccountEntity lopeAccount = getAccount(ACCOUNT_NUMBER_LOPE);
        Assertions.assertEquals(200, lopeAccount.getAmount(), "Unexpected amount on source account.");

        JournalEntity miguelJournal = getLatestJournalRecord(ACCOUNT_NUMBER_MIGUEL);
        Assertions.assertEquals(100, miguelJournal.getAmount(), "Unexpected journal amount.");
    }

    @Order(2)
    @Tag("QUARKUS-2492")
    @Test
    public void verifyLegacyNarayanaLambdaApproachTransaction() {
        TransferDTO transferDTO = new TransferDTO();
        transferDTO.setAccountFrom(ACCOUNT_NUMBER_GARCILASO);
        transferDTO.setAccountTo(ACCOUNT_NUMBER_GARCILASO);
        transferDTO.setAmount(100);

        given()
                .contentType(ContentType.JSON)
                .body(transferDTO).post("/transfer/legacy/top-up")
                .then().statusCode(HttpStatus.SC_CREATED);

        AccountEntity garcilasoAccount = getAccount(ACCOUNT_NUMBER_GARCILASO);
        Assertions.assertEquals(200, garcilasoAccount.getAmount(),
                "Unexpected account amount. Expected 200 found " + garcilasoAccount.getAmount());

        JournalEntity garcilasoJournal = getLatestJournalRecord(ACCOUNT_NUMBER_GARCILASO);
        Assertions.assertEquals(100, garcilasoJournal.getAmount(), "Unexpected journal amount.");
    }

    @Order(3)
    @Tag("QUARKUS-2492")
    @Test
    public void verifyNarayanaLambdaApproachTransaction() {
        makeTopUpTransfer();

        AccountEntity garcilasoAccount = getAccount(ACCOUNT_NUMBER_EDUARDO);
        Assertions.assertEquals(200, garcilasoAccount.getAmount(),
                "Unexpected account amount. Expected 200 found " + garcilasoAccount.getAmount());

        JournalEntity garcilasoJournal = getLatestJournalRecord(ACCOUNT_NUMBER_EDUARDO);
        Assertions.assertEquals(100, garcilasoJournal.getAmount(), "Unexpected journal amount.");
    }

    static void makeTopUpTransfer() {
        TransferDTO transferDTO = new TransferDTO();
        transferDTO.setAccountFrom(ACCOUNT_NUMBER_EDUARDO);
        transferDTO.setAccountTo(ACCOUNT_NUMBER_EDUARDO);
        transferDTO.setAmount(100);

        given()
                .contentType(ContentType.JSON)
                .body(transferDTO).post("/transfer/top-up")
                .then().statusCode(HttpStatus.SC_CREATED);
    }

    @Order(4)
    @Tag("QUARKUS-2492")
    @Test
    public void verifyRollbackForNarayanaProgrammaticApproach() {
        TransferDTO transferDTO = new TransferDTO();
        transferDTO.setAccountFrom(ACCOUNT_NUMBER_LUIS);
        transferDTO.setAccountTo(ACCOUNT_NUMBER_LUIS);
        transferDTO.setAmount(200);

        given()
                .contentType(ContentType.JSON)
                .body(transferDTO).post("/transfer/withdrawal")
                .then().statusCode(HttpStatus.SC_BAD_REQUEST);

        AccountEntity luisAccount = getAccount(ACCOUNT_NUMBER_LUIS);
        Assertions.assertEquals(100, luisAccount.getAmount(), "Unexpected account amount.");

        given().get("/transfer/journal/latest/" + ACCOUNT_NUMBER_LUIS)
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Order(5)
    @Tag("QUARKUS-2492")
    @Test
    public void smokeTestNarayanaProgrammaticTransactionTrace() {
        String operationName = "GET /transfer/accounts/{account_id}";
        given().get("/transfer/accounts/" + ACCOUNT_NUMBER_LUIS).then().statusCode(HttpStatus.SC_OK);
        verifyRequestTraces(operationName);
    }

    @Order(6)
    @Test
    public void verifyJdbcTraces() {
        for (String operationName : getExpectedJdbcOperationNames()) {
            verifyRequestTraces(operationName);
        }
    }

    protected String[] getExpectedJdbcOperationNames() {
        return new String[] { "SELECT mydb.account", "INSERT mydb.journal", "UPDATE mydb.account" };
    }

    @Order(7)
    @Tag("QUARKUS-2492")
    @Test
    public void smokeTestMetricsNarayanaProgrammaticTransaction() {
        String metricName = "transaction_withdrawal_amount";
        TransferDTO transferDTO = new TransferDTO();
        transferDTO.setAccountFrom(ACCOUNT_NUMBER_FRANCISCO);
        transferDTO.setAccountTo(ACCOUNT_NUMBER_FRANCISCO);
        transferDTO.setAmount(20);

        given()
                .contentType(ContentType.JSON)
                .body(transferDTO).post("/transfer/withdrawal")
                .then().statusCode(HttpStatus.SC_CREATED);

        verifyMetrics(metricName, greater(0));

        // check rollback gauge
        transferDTO.setAmount(3000);
        double beforeRollback = getMetricsValue(metricName);
        given()
                .contentType(ContentType.JSON)
                .body(transferDTO).post("/transfer/withdrawal")
                .then().statusCode(HttpStatus.SC_BAD_REQUEST);
        double afterRollback = getMetricsValue(metricName);
        Assertions.assertEquals(beforeRollback, afterRollback, "Gauge should not be increased on a rollback transaction");
    }

    @Order(8)
    @Tag("QUARKUS-2739")
    @Test
    public void testTransactionRecovery() {
        // make it possible to disable transaction recovery tests for certain databases
        testTransactionRecoveryInternal();
    }

    protected void testTransactionRecoveryInternal() {
        // test transactions without crash so that we check that on normal circumstances, there are no issues
        makeTransaction(false, false);
        // now make transaction sure transaction happened
        assertRecoveryLogContainsTransactions(2);
        // delete previous transactions so that we start for recovery from scratch
        deletePreviousTransactions();

        // test rollback only transaction is not committed; also by setting `crash` flag we verify that rollback
        // transactions are never written into JDBC object store, therefore they are not recovered either
        // (for crash flag only works during two-phase commit when JDBC Object store is in action)
        makeTransaction(true, true);
        assertRecoveryLogContainsTransactions(0);

        // test transactions recovery
        try {
            makeTransaction(false, true);
        } catch (Exception ignored) {
            // transaction crashed during two-phase commit, now we need to check that recovery_log is empty as planned
            getApp().withProperty(ENABLE_TRANSACTION_RECOVERY, FALSE.toString());
            getApp().restartAndWaitUntilServiceIsStarted();
            assertRecoveryLogContainsTransactions(0);
            assertJdbcObjectStoreContainsTransactions(1);

            // now enable automatic recovery and see the transaction recovered
            getApp().withProperty(ENABLE_TRANSACTION_RECOVERY, TRUE.toString());
            getApp().restartAndWaitUntilServiceIsStarted();
            // this might take a little while before periodic recovery module is started and run
            // default timeout should be fine, but if this happens to be flaky, we can safely raise the timeout
            untilAsserted(() -> assertRecoveryLogContainsTransactions(2));
        }
    }

    private void deletePreviousTransactions() {
        getApp().given()
                .contentType(ContentType.JSON)
                .delete(TRANSACTION_LOGS_PATH)
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        // make sure that delete worked
        assertRecoveryLogContainsTransactions(0);
    }

    private void makeTransaction(boolean rollback, boolean crash) {
        String transactionLogsPath = TRANSACTION_LOGS_PATH;
        if (crash) {
            transactionLogsPath += RECOVERY_SUBPATH;
        }
        getApp().given()
                .contentType(ContentType.JSON)
                .queryParam("rollback", rollback)
                .queryParam("executor", getTransactionExecutorUsedForRecovery())
                .post(transactionLogsPath)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(Matchers.is("2"));
    }

    private void assertRecoveryLogContainsTransactions(int number) {
        getApp().given()
                .contentType(ContentType.JSON)
                .get(TRANSACTION_LOGS_PATH)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(Matchers.is(Integer.toString(number)));
    }

    private void assertJdbcObjectStoreContainsTransactions(int number) {
        getApp().given()
                .contentType(ContentType.JSON)
                .get(TRANSACTION_LOGS_PATH + "/jdbc-object-store")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(Matchers.is(Integer.toString(number)));
    }

    private AccountEntity getAccount(String accountNumber) {
        return given().get("/transfer/accounts/" + accountNumber)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .body().as(AccountEntity.class);
    }

    private void verifyRequestTraces(String operationName) {
        verifyRequestTraces(operationName, jaeger);
    }

    static void verifyRequestTraces(String operationName, JaegerService jaeger) {
        await().atMost(1, TimeUnit.MINUTES).pollInterval(Duration.ofSeconds(5)).untilAsserted(() -> {
            var operations = getTracedOperationsForName(operationName, jaeger);
            Assertions.assertNotNull(operations);
            Assertions.assertTrue(operations.stream().anyMatch(operationName::equals));
        });
    }

    static List<String> getTracedOperationsForName(String operationName, JaegerService jaeger) {
        var jaegerResponse = retrieveTraces(20, "1h", "narayanaTransactions", operationName, jaeger);
        return jaegerResponse.jsonPath().getList("data[0].spans.operationName", String.class);
    }

    private JournalEntity getLatestJournalRecord(String accountNumber) {
        return given().get("/transfer/journal/latest/" + accountNumber)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .body().as(JournalEntity.class);
    }

    static Response retrieveTraces(int pageLimit, String lookBack, String serviceName, String operationName,
            JaegerService jaeger) {
        return given().when()
                .log().uri()
                .queryParam("operation", operationName)
                .queryParam("lookback", lookBack)
                .queryParam("limit", pageLimit)
                .queryParam("service", serviceName)
                .get(jaeger.getTraceUrl());
    }

    private void verifyMetrics(String name, Predicate<Double> valueMatcher) {
        await().ignoreExceptions().atMost(ASSERT_SERVICE_TIMEOUT_MINUTES, TimeUnit.MINUTES).untilAsserted(() -> {
            String response = given().get("/q/metrics").then()
                    .statusCode(HttpStatus.SC_OK)
                    .extract().asString();

            boolean matches = false;
            for (String line : response.split("[\r\n]+")) {
                if (line.startsWith(name)) {
                    Double value = extractValueFromMetric(line);
                    Assertions.assertTrue(valueMatcher.test(value), "Metric " + name + " has unexpected value " + value);
                    matches = true;
                    break;
                }
            }

            Assertions.assertTrue(matches, "Metric " + name + " not found in " + response);
        });
    }

    private Double getMetricsValue(String name) {
        String response = given().get("/q/metrics").then().statusCode(HttpStatus.SC_OK).extract().asString();
        for (String line : response.split("[\r\n]+")) {
            if (line.startsWith(name)) {
                return extractValueFromMetric(line);
            }
        }

        Assertions.fail("Metrics property " + name + " not found.");
        return 0d;
    }

    private Double extractValueFromMetric(String line) {
        return Double.parseDouble(line.substring(line.lastIndexOf(" ")));
    }

    private Predicate<Double> greater(double expected) {
        return actual -> actual > expected;
    }

}
