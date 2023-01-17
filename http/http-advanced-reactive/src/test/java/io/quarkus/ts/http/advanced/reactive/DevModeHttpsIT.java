package io.quarkus.ts.http.advanced.reactive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import io.quarkus.test.bootstrap.DevModeQuarkusService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusVersion;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.services.URILike;
import io.quarkus.test.utils.AwaitilityUtils;

@QuarkusScenario
@DisabledOnQuarkusVersion(version = "2\\.13\\.[0-6].*", reason = "Fixed in Quarkus 2.13.7")
public class DevModeHttpsIT {
    private static final String PROPERTY = "quarkus.qe.test.value";

    @DevModeQuarkusApplication(ssl = true)
    static RestService app = new DevModeQuarkusService()
            .withProperty("quarkus.oidc.enabled", "false")
            .withProperty("quarkus.keycloak.policy-enforcer.enable", "false")
            .withProperty("quarkus.keycloak.devservices.enabled", "false");

    private WebClient webClient;

    @BeforeEach
    void setUp() {
        webClient = new WebClient();

        webClient.getOptions().setRedirectEnabled(true); //required for the test case
        //The execution breaks without the two options below
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setWebSocketEnabled(false);

        //make sure, that the cache doesn't affect us
        webClient.getCookieManager().clearCookies();
        webClient.getCookieManager().setCookiesEnabled(false);
        webClient.getCache().clear();
        webClient.getCache().setMaxSize(0);

        //disable everything, that we don't need
        webClient.getOptions().setDownloadImages(false);
        webClient.getOptions().setGeolocationEnabled(false);
        webClient.getOptions().setAppletEnabled(false);
        webClient.getOptions().setCssEnabled(false);
    }

    @AfterEach
    void tearDown() {
        webClient.close();
    }

    @Test
    public void uiChange() throws IOException {
        URILike uri = app.getURI(Protocol.HTTPS);

        HtmlPage before = webClient.getPage(uri.withPath("/q/dev/io.quarkus.quarkus-vertx-http/config").toString());
        QuarkusUIField field = new QuarkusUIField(before.getElementById(PROPERTY));
        assertEquals("42", field.getValue());
        assertEquals("42", app.getProperty(PROPERTY, ""));

        field.setValue("23");
        HtmlPage saved = field.getSaveButton().click();
        QuarkusUIField updated = new QuarkusUIField(saved.getElementById(PROPERTY));
        assertEquals("23", updated.getValue());

        AwaitilityUtils.untilIsTrue(() -> app.getLogs().stream().anyMatch(log -> log.contains("File change detected")));
        assertEquals("23", app.getProperty(PROPERTY, ""));
    }
}

class QuarkusUIField {
    private final DomElement element;

    QuarkusUIField(DomElement element) {
        this.element = element;
    }

    public String getValue() {
        return element.getAttribute("value");
    }

    public void setValue(String newValue) {
        element.setAttribute("value", newValue);
    }

    public DomElement getSaveButton() {
        for (DomElement sibling : element.getParentNode().getDomElementDescendants()) {
            if (sibling.getAttribute("class").equals("input-group-text formInputButton")) {
                return sibling;
            }
        }
        throw new IllegalStateException("Save button was not found!");
    }
}
