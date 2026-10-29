package io.quarkus.ts.langchain4j.skills;

import static io.quarkus.ts.langchain4j.auxiliary.CommonTools.getKey;

import java.nio.file.Paths;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@Tag("QUARKUS-8960")
public class SkillsIT extends AbstractSkillsIT {

    @QuarkusApplication(classes = { SkillEndpoint.class, ProgrammaticToolProvider.class }, boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-skills") })
    static final RestService app = new RestService()
            .withProperty("quarkus.langchain4j.skills.directories", Paths.get(SKILLS_DIR).toAbsolutePath().toString())
            .withProperty("quarkus.langchain4j.openai.api-key", getKey());

    @Override
    protected RestService getApp() {
        return app;
    }

    @Override
    protected String getFinalSkillsDir() {
        return SKILLS_DIR;
    }
}
