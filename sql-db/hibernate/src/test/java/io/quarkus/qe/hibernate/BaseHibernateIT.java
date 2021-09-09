package io.quarkus.qe.hibernate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public abstract class BaseHibernateIT {

    private static final String TRANSACTION_SCOPE_BASE_PATH = "/transaction-scope";

    private static final String EXPECTED_RESPONSE_FROM_INVOKE_BEAN = "1";
    private static final String TRUE = Boolean.TRUE.toString();
    private static final String FALSE = Boolean.FALSE.toString();

    /**
     * Required data is pulled in from the `import.sql` resource.
     */
    @Test
    public void shouldNotFailWithConstraints() {
        given().when().get("/items/count").then().body(is("1"));
    }

    @Test
    public void shouldPostConstructAndPreDestroyBeInvoked() {
        givenPostConstructAndPreDestroyAreNotInvoked();
        whenInvokeBean();
        thenIsPostConstructInvoked();
        thenIsPreDestroyInvoked();
    }

    @Test
    public void testJpaAndHibernateValidatorEndpoint() {
        given()
                .when().get("/hello")
                .then()
                .body(containsString("hello"))
                .body(not(containsString("HV000041")));

        // second request is where the issue appears
        given()
                .when().get("/hello")
                .then()
                .body(containsString("hello"))
                .body(not(containsString("HV000041")))
                .body(not(containsString("HV000")));
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
