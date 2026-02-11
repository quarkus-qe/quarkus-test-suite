package io.quarkus.ts.security.oidcclient.mtls.service;

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
        this.realmBasePath = normalizeRealmBasePath(realmBasePath);
        this.openshiftScenario = TestExecutionProperties.isOpenshiftPlatform();
    }

    /**
     * {@link KeycloakService#getRealmUrl()} provides {@link Protocol#HTTP}, but this suite requires
     * {@link Protocol#HTTPS}.
     */
    @Override
    public String getRealmUrl() {
        return format("%s:%s/%s/%s", getURI(Protocol.HTTPS).getRestAssuredStyleUri(), getPort(), realmBasePath, realm);
    }

    public Integer getPort() {
        return openshiftScenario ? 443 : super.getURI().getPort();
    }

    private String normalizeRealmBasePath(String realmBasePath) {
        if (realmBasePath.startsWith("/")) {
            realmBasePath = realmBasePath.substring(1);
        }

        if (realmBasePath.endsWith("/")) {
            realmBasePath = realmBasePath.substring(0, realmBasePath.length() - 1);
        }

        return realmBasePath;
    }

}
