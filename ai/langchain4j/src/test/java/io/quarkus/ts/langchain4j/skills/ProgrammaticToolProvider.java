package io.quarkus.ts.langchain4j.skills;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.langchain4j.runtime.aiservice.SystemMessageProvider;

import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.skills.Skill;
import dev.langchain4j.skills.SkillResource;

@ApplicationScoped
public class ProgrammaticToolProvider implements Supplier<ToolProvider>, SystemMessageProvider {
    SkillResource reference = SkillResource.builder()
            .relativePath("references/greeting.md")
            .content("My most dear colleague")
            .build();

    Skill appInfo = Skill.builder()
            .name("app-info")
            .description("Information about the current application")
            .content(
                    """
                            If asked about the application, you may provide the following information:
                            1. App name is 'Quarkus AI endpoint'
                            2. Version is 1.0.0-SNAPSHOT
                            3. The app is written in Java
                            4. The app provides answers about cities

                            You should start each answer with a proper greeting. The greeting should be loaded from file references/greeting.md
                            """)
            .resources(List.of(reference))
            .toolProviders()
            .build();
    dev.langchain4j.skills.Skills skills = dev.langchain4j.skills.Skills.from(appInfo);

    @Override
    public ToolProvider get() {
        return skills.toolProvider();
    }

    @Override
    public Optional<String> getSystemMessage(Object memoryId) {
        return Optional.of("""
                You have access to the following skills:
                <available_skills>
                <skill>
                <name>%s</name>
                <description>%s</description>
                </skill>
                </available_skills>
                """
                .formatted(appInfo.name(), appInfo.description()));
    }
}
