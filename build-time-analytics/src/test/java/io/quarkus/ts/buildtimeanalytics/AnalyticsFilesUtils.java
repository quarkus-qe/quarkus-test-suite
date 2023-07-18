package io.quarkus.ts.buildtimeanalytics;

import java.io.File;
import java.nio.file.Path;

import io.quarkus.test.bootstrap.QuarkusCliRestService;
import io.quarkus.test.utils.FileUtils;

public class AnalyticsFilesUtils {
    private static final String USER_HOME = System.getProperty("user.home");
    private static final String CONFIG_DIR_NAME = ".redhat";
    private static final String LOCAL_CONFIG_FILE_NAME = "io.quarkus.analytics.localconfig";
    private static final String REMOTE_CONFIG_FILE_NAME = "io.quarkus.analytics.remoteconfig";
    private static final String APP_TARGET_DIR_NAME = "target";
    private static final String APP_ANALYTICS_PAYLOAD_FILE_NAME = "build-analytics-event.json";
    private static final String APP_ANALYTICS_POM_FILE_NAME = "pom.xml";
    private static final Path CONFIG_DIR_PATH = Path.of(USER_HOME, CONFIG_DIR_NAME);
    private static final Path LOCAL_CONFIG_PATH = Path.of(USER_HOME, CONFIG_DIR_NAME, LOCAL_CONFIG_FILE_NAME);
    private static final Path REMOTE_CONFIG_PATH = Path.of(USER_HOME, CONFIG_DIR_NAME, REMOTE_CONFIG_FILE_NAME);
    private static final Path APP_ANALYTICS_PAYLOAD_RELATIVE_PATH = Path.of(APP_TARGET_DIR_NAME,
            APP_ANALYTICS_PAYLOAD_FILE_NAME);
    private static final Path APP_ANALYTICS_POM_RELATIVE_PATH = Path.of(APP_ANALYTICS_POM_FILE_NAME);
    private static final String LOCAL_CONFIG_ENABLED_RESOURCE = "local-config-enabled.json";
    private static final String LOCAL_CONFIG_DISABLED_RESOURCE = "local-config-disabled.json";
    private static final String REMOTE_CONFIG_ENABLED_RESOURCE = "remote-config-enabled.json";
    private static final String REMOTE_CONFIG_DISABLED_RESOURCE = "remote-config-disabled.json";
    private static final String REMOTE_CONFIG_REFRESHABLE_RESOURCE = "remote-config-refreshable.json";

    public static void deleteConfigDir() {
        FileUtils.deletePath(CONFIG_DIR_PATH);
    }

    public static void recreateConfigDir() {
        FileUtils.recreateDirectory(CONFIG_DIR_PATH);
        FileUtils.copyFileTo(LOCAL_CONFIG_ENABLED_RESOURCE, LOCAL_CONFIG_PATH);
        FileUtils.copyFileTo(REMOTE_CONFIG_ENABLED_RESOURCE, REMOTE_CONFIG_PATH);
    }

    public static void disableAnalyticsByLocalConfig() {
        FileUtils.copyFileTo(LOCAL_CONFIG_DISABLED_RESOURCE, LOCAL_CONFIG_PATH);
    }

    public static void disableAnalyticsByRemoteConfig() {
        FileUtils.copyFileTo(REMOTE_CONFIG_DISABLED_RESOURCE, REMOTE_CONFIG_PATH);
    }

    public static void useRefreshableRemoteConfig() {
        FileUtils.copyFileTo(REMOTE_CONFIG_REFRESHABLE_RESOURCE, REMOTE_CONFIG_PATH);
    }

    public static File getRemoteConfigFile() {
        return REMOTE_CONFIG_PATH.toFile();
    }

    public static File getAnalyticsPayloadFile(QuarkusCliRestService app) {
        return getFileFromApp(app, APP_ANALYTICS_PAYLOAD_RELATIVE_PATH);
    }

    public static File getPomFile(QuarkusCliRestService app) {
        return getFileFromApp(app, APP_ANALYTICS_POM_RELATIVE_PATH);
    }

    private static File getFileFromApp(QuarkusCliRestService app, Path relativeFilePath) {
        return app.getServiceFolder().resolve(relativeFilePath).toFile();
    }
}
