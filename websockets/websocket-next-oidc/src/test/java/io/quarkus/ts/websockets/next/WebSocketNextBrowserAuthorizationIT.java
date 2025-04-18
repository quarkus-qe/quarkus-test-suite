package io.quarkus.ts.websockets.next;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.URILike;

@QuarkusScenario
public class WebSocketNextBrowserAuthorizationIT {

    private static final String ADMIN_USERNAME = "charlie";
    private static final String ADMIN_PASSWORD = "random";
    private static final String USER_USERNAME = "albert";
    private static final String USER_PASSWORD = "einstein";
    private ObjectMapper objectMapper;
    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext browserContext;
    private Page page;
    private URILike wsBaseUri;
    private URILike wssBaseUri;

    @KeycloakContainer(command = { "start-dev", "--import-realm", "--features=token-exchange" })
    static KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH);

    @QuarkusApplication(ssl = true, certificates = {
            @Certificate(configureKeystore = true, configureTruststore = true, useTlsRegistry = false, configureHttpServer = true)
    })
    static final RestService server = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", keycloak::getRealmUrl);

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true));
    }

    @BeforeEach
    void setWsBaseUri() {
        Browser.NewContextOptions options = new Browser.NewContextOptions()
                .setIgnoreHTTPSErrors(true);
        browserContext = browser.newContext(options);
        page = browserContext.newPage();
        wsBaseUri = server.getURI(Protocol.WS);
        wssBaseUri = server.getURI(Protocol.WSS);
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
    }

    @AfterEach
    void teardownEach() {
        if (browserContext != null) {
            browserContext.close();
        }
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    private String getBearerToken(String username, String password) {
        return keycloak.createAuthzClient("test-application-client", "test-application-client-secret")
                .obtainAccessToken(username, password).getToken();
    }

    private String getEncodedAuthSubprotocol(String username, String password) {
        String token = getBearerToken(username, password);
        String subprotocol = "quarkus-http-upgrade#Authorization#Bearer " + token;
        return URLEncoder.encode(subprotocol, StandardCharsets.UTF_8);
    }

    /**
     * Convert JavaScript evaluation result to a typed Map using jackson.databind.ObjectMapper
     */
    private <T> T evaluateAndConvert(String script, Object[] args, TypeReference<T> typeRef) {

        Object rawResult = page.evaluate(script, args);

        if (rawResult == null) {
            return null;
        }

        // Convert the result
        return objectMapper.convertValue(rawResult, typeRef);
    }

    @Test
    void successfulAuthorizationAndSubprotocolNegotiationWSS() {
        // Encoding is necessary to avoid issues with special characters
        String encodedSubprotocol = getEncodedAuthSubprotocol(ADMIN_USERNAME, ADMIN_PASSWORD);
        String wssUrl = wssBaseUri.withPath("/bearer").toString();

        Map<String, Object> result = evaluateAndConvert(
                """
                        // This JS function establishes a secure WebSocket connection using 2 subbprotocols 'bearer-token-carrier'
                        // and the subprotocol with the encoded authorization
                        ([wssUrl, subprotocol]) => {
                            return new Promise((resolve, reject) => {
                                try {
                                    const ws = new WebSocket(wssUrl, ['bearer-token-carrier', subprotocol]);
                                    let messages = [];
                                    ws.onopen = () => {
                                        // Store the protocol directly when connection opens
                                        const negotiatedProtocol = ws.protocol;
                                        console.log("Negotiated protocol:", negotiatedProtocol);
                                        ws.send('Hello server');
                                    };
                                    ws.onmessage = (event) => {
                                        messages.push(event.data);
                                        if (messages.length === 1) {
                                            setTimeout(() => {
                                                // Include the protocol in the result object
                                                resolve({
                                                    status: 'open',
                                                    messages: messages,
                                                    protocol: ws.protocol
                                                });
                                                ws.close();
                                            }, 1000);
                                        }
                                    };
                                    ws.onerror = () => resolve({ status: 'error' });
                                    ws.onclose = (event) => {
                                        if (messages.length === 0) {
                                            resolve({ status: 'closed', code: event.code });
                                        }
                                    };
                                } catch(e) {
                                    reject(e.message);
                                }
                            });
                        }
                        """,
                new Object[] { wssUrl, encodedSubprotocol },
                new TypeReference<>() {
                });

        assert result != null;
        assertEquals("open", result.get("status"), "WebSocket connection should be open with valid token");

        // Verify that the correct protocol has been negotiated
        String negotiatedProtocol = (String) result.get("protocol");
        assertTrue(negotiatedProtocol != null && !negotiatedProtocol.isEmpty(),
                "Server should negotiate a protocol");
        assertEquals("bearer-token-carrier", negotiatedProtocol,
                "Server should select 'bearer-token-carrier' protocol");

        List<String> messages = objectMapper.convertValue(result.get("messages"), new TypeReference<>() {
        });
        assertTrue(messages != null && !messages.isEmpty(), "Should receive at least one message");
    }

    @Test
    void successfulAuthorizationWS() {
        String encodedSubprotocol = getEncodedAuthSubprotocol(ADMIN_USERNAME, ADMIN_PASSWORD);
        String wsUrl = wsBaseUri.withPath("/bearer").toString();

        Map<String, Object> result = evaluateAndConvert(
                """
                        ([wsUrl, subprotocol]) => {
                            return new Promise((resolve, reject) => {
                                try {
                                    const ws = new WebSocket(wsUrl, ['bearer-token-carrier', subprotocol]);
                                    ws.onopen = () => {
                                        setTimeout(() => {
                                            resolve({ status: 'open' });
                                            ws.close();
                                        }, 1000);
                                    };
                                    ws.onerror = () => resolve({ status: 'error' });
                                    ws.onclose = (event) => resolve({ status: 'closed', code: event.code });
                                } catch(e) {
                                    reject(e.message);
                                }
                            });
                        }
                        """,
                new Object[] { wsUrl, encodedSubprotocol },
                new TypeReference<>() {
                });

        assert result != null;
        assertEquals("open", result.get("status"), "WebSocket connection should be open with valid token");
    }

    @Test
    void failedAuthorizationWithoutSubprotocolWSS() {
        String wssUrl = wssBaseUri.withPath("/bearer").toString();

        Map<String, Object> result = evaluateAndConvert(
                """
                        (wsUrl) => {
                                     return new Promise((resolve) => {
                                           const ws = new WebSocket(wsUrl);
                                           ws.onopen = () => {
                                             setTimeout(() => {
                                               resolve({ status: 'open' });
                                               ws.close();
                                             }, 1000);
                                           };
                                           ws.onerror = () => {};
                                           ws.onclose = (closeEvent) => {
                                             resolve({ status: 'error', code: closeEvent.code });
                                           }
                                         });
                                        }
                        """,
                new Object[] { wssUrl },
                new TypeReference<>() {
                });

        assert result != null;
        assertEquals("error", result.get("status"), "Connection should fail without authorization");
        assertEquals(1006, ((Number) result.get("code")).intValue(), "Expected error code for unauthorized connection");

    }

    @Test
    void failedAuthenticationWithInvalidToken() {
        String invalidToken = "invalid_token_format";
        String wssUrl = wssBaseUri.withPath("/bearer").toString();

        String subprotocol = "quarkus-http-upgrade#Authorization#Bearer " + invalidToken;
        String encodedSubprotocol = URLEncoder.encode(subprotocol, StandardCharsets.UTF_8);

        Map<String, Object> result = evaluateAndConvert(
                """
                        ([wsUrl, subprotocol]) => {
                            return new Promise((resolve) => {
                                const ws = new WebSocket(wsUrl, ['bearer-token-carrier', subprotocol]);
                                ws.onopen = () => {
                                    setTimeout(() => {
                                        resolve({ status: 'open' });
                                        ws.close();
                                    }, 1000);
                                };
                                ws.onerror = () => {};
                                ws.onclose = (closeEvent) => {
                                    resolve({ status: 'error', code: closeEvent.code });
                                };
                            });
                        }
                        """,
                new Object[] { wssUrl, encodedSubprotocol },
                new TypeReference<>() {
                });

        assertEquals("error", result.get("status"), "Connection should fail with invalid token");
        assertEquals(1006, ((Number) result.get("code")).intValue(), "Expected error code for invalid token");

    }

    @Test
    void failedAuthorizationUnauthorizedToken() {
        String encodedSubprotocol = getEncodedAuthSubprotocol(USER_USERNAME, USER_PASSWORD);
        String wssUrl = wssBaseUri.withPath("/protected").toString();

        Map<String, Object> result = evaluateAndConvert(
                """
                        ([wsUrl, subprotocol]) => {
                            return new Promise((resolve) => {
                                const ws = new WebSocket(wsUrl, ['bearer-token-carrier', subprotocol]);
                                ws.onopen = () => {
                                    setTimeout(() => {
                                        resolve({ status: 'open' });
                                        ws.close();
                                    }, 1000);
                                };
                                ws.onerror = () => {};
                                ws.onclose = (closeEvent) => {
                                    resolve({ status: 'error', code: closeEvent.code });
                                };
                            });
                        }
                        """,
                new Object[] { wssUrl, encodedSubprotocol },
                new TypeReference<>() {
                });

        assert result != null;
        assertEquals("error", result.get("status"), "Connection should fail with unauthorized token");
        assertEquals(1006, ((Number) result.get("code")).intValue(), "Expected error code for unauthorized token");
    }

    @Test
    void verifyCorrectHandlingPostSuccessfulAuthorization() {
        final int MESSAGE_COUNT = 3;
        final int TIMEOUT_MS = 5000;
        String encodedSubprotocol = getEncodedAuthSubprotocol(ADMIN_USERNAME, ADMIN_PASSWORD);
        String wssUrl = wssBaseUri.withPath("/bearer").toString();

        Map<String, Object> result = evaluateAndConvert(
                """
                        // This function tests behavior after successful authorization
                        // by sending multiple messages to verify stability
                        ([wsUrl, subprotocol, expectedMessageCount, timeoutMs]) => {
                            return new Promise((resolve, reject) => {
                                const messages = [];
                                let negotiatedProtocol = null;
                                try {
                                    const ws = new WebSocket(wsUrl, ['bearer-token-carrier', subprotocol]);
                                    ws.onopen = () => {
                                        // Store protocol when connection opens
                                        negotiatedProtocol = ws.protocol;
                                        console.log("Negotiated protocol:", negotiatedProtocol);
                                        // Send multiple messages
                                        ws.send('Message 1');
                                        setTimeout(() => ws.send('Message 2'), 500);
                                        setTimeout(() => ws.send('Message 3'), 1000);
                                    };
                                    ws.onmessage = (event) => {
                                        messages.push(event.data);
                                        // After the third message, close and resolve
                                        if (messages.length === expectedMessageCount) {
                                            setTimeout(() => {
                                                resolve({
                                                    status: 'success',
                                                    messages: messages,
                                                    protocol: negotiatedProtocol
                                                });
                                                ws.close();
                                            }, 1000);
                                        }
                                    };
                                    ws.onerror = () => reject({ status: 'error', message: 'Connection error' });
                                    // Set a maximum wait time
                                    setTimeout(() => {
                                        if (messages.length < expectedMessageCount) {
                                            resolve({
                                                status: 'timeout',
                                                messages: messages,
                                                protocol: negotiatedProtocol
                                            });
                                            ws.close();
                                        }
                                    }, timeoutMs);
                                } catch(e) {
                                    reject({ status: 'error', message: e.message });
                                }
                            });
                        }
                        """,
                new Object[] { wssUrl, encodedSubprotocol, MESSAGE_COUNT, TIMEOUT_MS },
                new TypeReference<>() {
                });

        assert result != null;
        assertEquals("success", result.get("status"), "WebSocket connection should remain stable after authentication");

        List<String> messages = objectMapper.convertValue(result.get("messages"), new TypeReference<>() {
        });
        assertEquals(MESSAGE_COUNT, messages.size(), "Should receive all " + MESSAGE_COUNT + " sent messages");

        for (String message : messages) {
            assertTrue(message.contains(ADMIN_USERNAME),
                    "Each message should contain the authenticated username: " + message);
        }

        // Verify that the negotiated protocol is as expected
        String negotiatedProtocol = (String) result.get("protocol");
        assertEquals("bearer-token-carrier", negotiatedProtocol,
                "Server should select 'bearer-token-carrier' protocol");
    }

}
