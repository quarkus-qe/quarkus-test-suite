package io.quarkus.qe.hibernate.transaction;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;

@QuarkusScenario
public class TransactionScopeBeanResourceIT {

    private static final String TRANSACTION_SCOPE_BASE_PATH = "/transaction-scope";

    private static final String EXPECTED_RESPONSE_FROM_INVOKE_BEAN = "1";
    private static final String TRUE = Boolean.TRUE.toString();
    private static final String FALSE = Boolean.FALSE.toString();

    @DevModeQuarkusApplication
    //TODO: simplify when https://github.com/quarkus-qe/quarkus-test-framework/issues/249 is resolved
    static RestService app = new RestService().onPostStart(app -> {
        app.logs().assertContains("Listening on");
    });

    @Test
    public void shouldPostConstructAndPreDestroyBeInvoked() {
        givenPostConstructAndPreDestroyAreNotInvoked();
        whenInvokeBean();
        thenIsPostConstructInvoked();
        thenIsPreDestroyInvoked();
    }

    private void givenPostConstructAndPreDestroyAreNotInvoked() {
        assertEquals(FALSE, getPostConstructInvokeResult(), "PostConstruct method has been invoked already");
        assertEquals(FALSE, getPreDestroyInvokeResult(), "PreDestroy method has been invoked already");
    }

    private void whenInvokeBean() {
        given().when().post(transactionScopePath("/invoke-bean")).then().body(is(EXPECTED_RESPONSE_FROM_INVOKE_BEAN));
    }

    private void thenIsPostConstructInvoked() {
        assertEquals(TRUE, getPostConstructInvokeResult(), "PostConstruct method is not invoked");
    }

    private void thenIsPreDestroyInvoked() {
        assertEquals(TRUE, getPreDestroyInvokeResult(), "PreDestroy method is not invoked");
    }

    private String getPostConstructInvokeResult() {
        return given().when().get(transactionScopePath("/is-post-construct-invoked")).asString();
    }

    private String getPreDestroyInvokeResult() {
        return given().when().get(transactionScopePath("/is-pre-destroy-invoked")).asString();
    }

    private String transactionScopePath(String path) {
        return TRANSACTION_SCOPE_BASE_PATH + path;
    }
}
