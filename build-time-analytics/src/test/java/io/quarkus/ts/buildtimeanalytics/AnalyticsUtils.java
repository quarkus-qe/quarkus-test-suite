package io.quarkus.ts.buildtimeanalytics;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.quarkus.test.services.quarkus.model.QuarkusProperties;

public class AnalyticsUtils {
    private static final String QUARKUS_SNAPSHOT_VERSION = "999-SNAPSHOT";
    // Quarkus reports extensions' versions in the analytics payload as full artifact version.
    // So for 999-SNAPSHOT, it can look like e.g. 999-20230718.203351-1091
    private static final String QUARKUS_EXTENSION_SNAPSHOT_VERSION_REGEX = "999-.*";
    // RHBQ artifacts may differ in the number suffix from a platform version.
    // E.g.: 3.8.0.redhat-00002 vs. 3.8.5.redhat-00003
    private static final String RHBQ_VERSION_REGEX_FORMAT = "%s\\.redhat-\\d{5}";
    public static final Pattern QUARKUS_EXTENSION_VERSION_PATTERN = Pattern.compile(getExtensionVersionRegex());

    public static final String QUARKUS_ANALYTICS_DISABLED_PROPERTY = "quarkus.analytics.disabled";
    public static final String QUARKUS_ANALYTICS_URI_BASE_PROPERTY = "quarkus.analytics.uri.base";
    public static final String QUARKUS_ANALYTICS_TIMEOUT_PROPERTY = "quarkus.analytics.timeout";
    public static final String QUARKUS_ANALYTICS_FAKE_URI_BASE = "http://fake.url";
    public static final String QUARKUS_ANALYTICS_EVENT_DEV = "DEV_MODE";
    public static final String QUARKUS_ANALYTICS_EVENT_PROD = "BUILD";
    public static final String QUARKUS_ANALYTICS_IP = "0.0.0.0";
    // Denied groupIds defined by https://github.com/quarkusio/quarkus/blob/main/independent-projects/tools/analytics-common/src/main/java/io/quarkus/analytics/config/GroupIdFilter.java
    public static final String APP_NAME_WITH_DENIED_GROUP_ID = "io.quarkus.qe:app";
    public static final String APP_NAME_WITH_NON_DENIED_GROUP_ID = "rh.quarkus.qe:app";
    public static final String UNRECOGNIZED_PROPERTY_FORMAT = "Unrecognized configuration key \"%s\" was provided";
    public static final String ANALYTICS_ACTIVATION_LINK = "https://quarkus.io/usage";
    public static final String ANALYTICS_ACTIVATION_PROMPT = "Do you agree to contribute anonymous build time data to the Quarkus community? (y/n)";
    public static final String[] EXTENSION_SET_A = new String[] {
            "quarkus-agroal",
            "quarkus-arc",
            "quarkus-cache",
            "quarkus-config-yaml",
            "quarkus-core",
            "quarkus-grpc",
            "quarkus-hibernate-orm",
            "quarkus-hibernate-orm-panache",
            "quarkus-hibernate-validator",
            "quarkus-infinispan-client",
            "quarkus-jackson",
            "quarkus-jaxb",
            "quarkus-jdbc-mysql",
            "quarkus-jdbc-postgresql",
            "quarkus-jsonb",
            "quarkus-jsonp",
            "quarkus-kafka-client",
            "quarkus-micrometer",
            "quarkus-micrometer-registry-prometheus",
            "quarkus-narayana-jta",
            "quarkus-oidc",
            "quarkus-openshift-client",
            "quarkus-quartz",
            "quarkus-reactive-pg-client",
            "quarkus-reactive-routes",
            "quarkus-resteasy-client",
            "quarkus-resteasy-client-jaxb",
            "quarkus-resteasy",
            "quarkus-resteasy-jackson",
            "quarkus-resteasy-jaxb",
            "quarkus-resteasy-jsonb",
            "quarkus-scheduler",
            "quarkus-smallrye-graphql-client",
            "quarkus-smallrye-reactive-streams-operators",
            "quarkus-spring-boot-properties",
            "quarkus-spring-data-jpa",
            "quarkus-spring-di",
            "quarkus-spring-security",
            "quarkus-undertow",
            "quarkus-vertx",
    };
    public static final String[] EXTENSION_SET_B = new String[] {
            "quarkus-avro",
            "quarkus-container-image-openshift",
            "quarkus-hibernate-orm-rest-data-panache",
            "quarkus-rest-client-jaxrs",
            "quarkus-jdbc-mariadb",
            "quarkus-jdbc-mssql",
            "quarkus-mutiny",
            "quarkus-oidc-client",
            "quarkus-rest-client-oidc-filter",
            "quarkus-qute",
            "quarkus-reactive-mysql-client",
            "quarkus-rest-client",
            "quarkus-rest",
            "quarkus-rest-jackson",
            "quarkus-smallrye-context-propagation",
            "quarkus-smallrye-fault-tolerance",
            "quarkus-smallrye-health",
            "quarkus-smallrye-jwt",
            "quarkus-smallrye-jwt-build",
            "quarkus-smallrye-metrics",
            "quarkus-smallrye-openapi",
            "quarkus-opentelemetry",
            "quarkus-messaging",
            "quarkus-messaging-kafka",
            "quarkus-spring-cache",
            "quarkus-spring-cloud-config-client",
            "quarkus-spring-data-rest",
            "quarkus-spring-scheduled",
            "quarkus-spring-web",
            "quarkus-websockets",
            "quarkus-websockets-client",
    };

    private static String getExtensionVersionRegex() {
        String version = QuarkusProperties.getVersion();
        Matcher rhbqMacher = Pattern.compile(String.format(RHBQ_VERSION_REGEX_FORMAT, "(.*)")).matcher(version);
        if (rhbqMacher.matches()) {
            return String.format(RHBQ_VERSION_REGEX_FORMAT, rhbqMacher.group(1));
        }
        if (version.equalsIgnoreCase(QUARKUS_SNAPSHOT_VERSION)) {
            return QUARKUS_EXTENSION_SNAPSHOT_VERSION_REGEX;
        }
        return version;
    }
}
