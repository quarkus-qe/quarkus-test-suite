package io.quarkus.ts.security.oidcclient.mtls;

import static io.quarkus.test.utils.PropertiesUtils.SECRET_PREFIX;
import static java.lang.String.format;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.Protocol;

public class MutualTlsKeycloakService extends KeycloakService {

    private static final String X509_CA_BUNDLE = "X509_CA_BUNDLE";

    private final String realm;
    private final String realmBasePath;
    private final boolean openshiftScenario;

    // command used by Keycloak 18+ container in order to launch JKS secured Keycloak
    public static final String KC_DEV_MODE_JKS_CMD = "start-dev " +
            "--import-realm --hostname-strict=false --hostname-strict-https=false --features=token-exchange " +
            "--https-client-auth=required " +
            "--https-key-store-file=/etc/server-keystore.jks " +
            "--https-trust-store-file=/etc/server-truststore.jks " +
            "--https-trust-store-password=password";

    // command used by Keycloak 18+ container in order to launch P12 secured Keycloak
    public static final String KC_DEV_MODE_P12_CMD = "start-dev " +
            "--import-realm --hostname-strict=false --hostname-strict-https=false --features=token-exchange " +
            "--https-client-auth=required " +
            "--https-key-store-file=/etc/server-keystore.p12 " +
            "--https-trust-store-file=/etc/server-truststore.p12 " +
            "--https-trust-store-password=password";

    public static MutualTlsKeycloakService newKeycloakInstance(String realmFile, String realmName, String realmBasePath) {
        return new MutualTlsKeycloakService(realmFile, realmName, realmBasePath);
    }

    public static MutualTlsKeycloakService newRhSsoInstance(String realmFilePath, String realm) {
        return (MutualTlsKeycloakService) new MutualTlsKeycloakService(realm)
                .withProperty(X509_CA_BUNDLE, "/var/run/secrets/kubernetes.io/serviceaccount/*.crt")
                .withProperty("SSO_IMPORT_FILE", SECRET_PREFIX + realmFilePath);
    }

    private MutualTlsKeycloakService(String realmFile, String realmName, String realmBasePath) {
        super(realmFile, realmName, realmBasePath);
        this.realm = realmName;
        this.realmBasePath = realmBasePath;
        openshiftScenario = false;
    }

    private MutualTlsKeycloakService(String realm) {
        super(realm);
        openshiftScenario = true;
        this.realm = realm;
        this.realmBasePath = "auth/realms";
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
        return format("%s:%s/%s/%s", getHost(Protocol.HTTPS), getPort(), realmBasePath, realm);
    }

    @Override
    public Integer getPort() {
        return openshiftScenario ? 443 : super.getPort();
    }

}
