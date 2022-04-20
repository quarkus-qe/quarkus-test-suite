package io.quarkus.ts.security.oidcclient.mtls;

import static io.quarkus.test.utils.PropertiesUtils.RESOURCE_PREFIX;
import static io.quarkus.test.utils.PropertiesUtils.SECRET_PREFIX;
import static java.lang.String.format;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.Protocol;

public class MutualTlsKeycloakService extends KeycloakService {

    private static final String REALM_DEFAULT = "test-mutual-tls-realm";
    private static final String CERT_PATH = "/etc/x509/https/";
    private static final String MUTUAL_TLS_BUNDLE_PATH = CERT_PATH + "ca-client.bundle";
    private static final String TLS_CRT_PATH = CERT_PATH + "tls.crt";
    private static final String TLS_KEY_PATH = CERT_PATH + "tls.key";
    private static final String CREATE_REALM_FILE_PATH = "/keycloak-realm.json";
    private static final String X509_CA_BUNDLE = "X509_CA_BUNDLE";
    private final boolean openshiftScenario;
    private final String realm;

    private MutualTlsKeycloakService(String createKeycloakRealmPath, String realm) {
        super(createKeycloakRealmPath, realm);
        openshiftScenario = false;
        this.realm = realm;
    }

    private MutualTlsKeycloakService(String realm) {
        super(realm);
        openshiftScenario = true;
        this.realm = realm;
    }

    public static KeycloakService newKeycloakInstance(String realmFilePath, String realm) {
        return new MutualTlsKeycloakService(realmFilePath, realm)
                .withRedHatFipsDisabled()
                .withProperty("MOUNT_CA_BUNDLE", RESOURCE_PREFIX + MUTUAL_TLS_BUNDLE_PATH)
                .withProperty("MOUNT_TLS_CRT", RESOURCE_PREFIX + TLS_CRT_PATH)
                .withProperty("MOUNT_TLS_KEY", RESOURCE_PREFIX + TLS_KEY_PATH)
                .withProperty(X509_CA_BUNDLE, MUTUAL_TLS_BUNDLE_PATH);
    }

    public static KeycloakService newKeycloakInstance() {
        return newKeycloakInstance(CREATE_REALM_FILE_PATH, REALM_DEFAULT);
    }

    public static MutualTlsKeycloakService newRhSsoInstance(String realmFilePath, String realm) {
        return (MutualTlsKeycloakService) new MutualTlsKeycloakService(realm)
                .withProperty(X509_CA_BUNDLE, "/var/run/secrets/kubernetes.io/serviceaccount/*.crt")
                .withProperty("SSO_IMPORT_FILE", SECRET_PREFIX + realmFilePath);
    }

    public static MutualTlsKeycloakService newRhSsoInstance() {
        return newRhSsoInstance(CREATE_REALM_FILE_PATH, REALM_DEFAULT);
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
        return format("%s:%s/auth/realms/%s", getHost(Protocol.HTTPS), getPort(), realm);
    }

    /**
     * OpenShift 'passThrough' route listens on 443 and redirects to internal port 8443.
     */
    @Override
    public Integer getPort() {
        return openshiftScenario ? 443 : super.getPort();
    }

}
