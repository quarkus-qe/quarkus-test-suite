package io.quarkus.qe.hibernate;

public class DockerUtils {

    static final String DOCKER_IP = "DOCKER_IP";

    // TODO: move this to the framework
    static boolean isNotPodman() {
        final String dockerHostEnvVar = System.getenv("DOCKER_HOST");
        return dockerHostEnvVar == null || !dockerHostEnvVar.contains("podman");
    }

}
