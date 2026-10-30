package io.quarkus.ts.langchain4j.skills;

import static io.quarkus.ts.langchain4j.auxiliary.CommonTools.getKey;

import java.nio.file.Path;
import java.nio.file.Paths;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.Build)
public class OpenShiftSkillsIT extends AbstractSkillsIT {
    private static final String MOUNTED_SKILLS = "/tmp/skills";

    @QuarkusApplication(classes = { SkillEndpoint.class, ProgrammaticToolProvider.class }, boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-skills") })
    static final RestService app = new RestService()
            .withProperty("_ignored", "resource_with_destination::%s/temperature/|%s"
                    .formatted(MOUNTED_SKILLS, getPathToSkillFile("temperature/SKILL.md")))
            .withProperty("_also_ignored", "resource_with_destination::%s/temperature/|%s"
                    .formatted(MOUNTED_SKILLS, getPathToSkillFile("temperature/weather.csv")))
            .withProperty("quarkus.langchain4j.skills.directories", "/tmp/skills/")
            .withProperty("quarkus.langchain4j.openai.api-key", getKey());

    private static Path getPathToSkillFile(String file) {
        return Paths.get(SKILLS_DIR).resolve(file).toAbsolutePath();
    }

    @Override
    protected String getFinalSkillsDir() {
        return MOUNTED_SKILLS;
    }

    @Override
    RestService getApp() {
        return app;
    }
}
