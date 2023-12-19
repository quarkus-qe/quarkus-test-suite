package io.quarkus.ts.security.oidcclient.mtls;

import static java.lang.String.format;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.utils.TestExecutionProperties;

public class MutualTlsKeycloakService extends KeycloakService {

    private static final String X509_CA_BUNDLE = "X509_CA_BUNDLE";

    private final String realm;
    private final String realmBasePath;
    private final boolean openshiftScenario;

    public static MutualTlsKeycloakService newKeycloakInstance(String realmFile, String realmName, String realmBasePath) {
        return new MutualTlsKeycloakService(realmFile, realmName, realmBasePath);
    }

    public static MutualTlsKeycloakService newRhSsoInstance(String realmFile, String realm) {
        return (MutualTlsKeycloakService) new MutualTlsKeycloakService(realmFile, realm, DEFAULT_REALM_BASE_PATH)
                .withProperty(X509_CA_BUNDLE, "/var/run/secrets/kubernetes.io/serviceaccount/*.crt");
    }

    private MutualTlsKeycloakService(String realmFile, String realmName, String realmBasePath) {
        super(realmFile, realmName, realmBasePath);
        this.realm = realmName;
        this.realmBasePath = realmBasePath;
        this.openshiftScenario = TestExecutionProperties.isOpenshiftPlatform();
    }

    /**
     * TODO Remove workaround after Keycloak is fixed https://github.com/keycloak/keycloak/issues/9916
     */
    public KeycloakService withRedHatFipsDisabled() {
        withProperty("JAVA_OPTS", "-Dcom.redhat.fips=false");
        return this;
    }

    /**
     * {@link KeycloakService#getRealmUrl()} provides {@link Protocol#HTTP}, but this suite requires
     * {@link Protocol#HTTPS}.
     */
    @Override
    public String getRealmUrl() {
        return format("%s:%s/%s/%s", getURI(Protocol.HTTPS).getRestAssuredStyleUri(), getPort(), realmBasePath, realm);
    }

    @Override
    public Integer getPort() {
        return openshiftScenario ? 443 : super.getURI().getPort();
    }

}
