package io.quarkus.qe.scheduling.spring;

import jakarta.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scheduler/count")
public class CountResource {

    @Autowired
    AnnotationScheduledCounter annotationScheduledcounter;

    @GetMapping(value = "annotation", produces = MediaType.TEXT_PLAIN)
    public Integer getAnnotationCount() {
        return annotationScheduledcounter.get();
    }
}
