package io.quarkus.ts.sqldb.panacheflyway.dbpool;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

public class AgroalTestProfile implements QuarkusTestProfile {

    public static final String PROFILE = "agroal_pool_test";

    @Override
    public String getConfigProfile() {
        return PROFILE;
    }

    @Override
    public List<TestResourceEntry> testResources() {
        return Collections.singletonList(new TestResourceEntry(MySqlDatabaseTestResource.class));
    }

    @Override
    public Map<String, String> getConfigOverrides() {
        Map<String, String> config = new HashMap<>();
        config.put("quarkus.datasource.devservices.enabled", "false");
        config.put("quarkus.datasource.with-xa.devservices.enabled", "false");
        return config;
    }
}
