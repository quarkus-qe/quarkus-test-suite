package io.quarkus.ts.spring.web.bootstrap.web;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@RestController
public class SimpleController {

    @Value("${spring.application.name}")
    String appName;

    @Inject
    Template home;

    @RequestMapping(value = "/", produces = MediaType.TEXT_HTML)
    public TemplateInstance homePage() {
        return home.data("appName", appName);
    }
}
