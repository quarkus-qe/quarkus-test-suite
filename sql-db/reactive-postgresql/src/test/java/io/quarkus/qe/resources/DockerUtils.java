package io.quarkus.qe.resources;

import java.util.List;
import java.util.Optional;

import org.testcontainers.DockerClientFactory;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Image;

// TODO this DockerUtils must be removed once this changes will be released
// https://github.com/quarkus-qe/quarkus-test-framework/blob/main/quarkus-test-images/src/main/java/io/quarkus/test/utils/DockerUtils.java
public class DockerUtils {

    private static final DockerClient DOCKER_CLIENT = DockerClientFactory.instance().client();;

    /**
     * Returns true if docker images contains expectedVersion.
     *
     * @param image docker images
     * @param expectedVersion expected docker image version
     */
    public static boolean isVersion(Image image, String expectedVersion) {
        boolean exist = false;
        String[] tags = Optional.ofNullable(image.getRepoTags()).orElse(new String[] {});
        for (String tag : tags) {
            if (tag.contains(expectedVersion)) {
                exist = true;
                break;
            }
        }

        return exist;
    }

    /**
     * Returns true if docker image is removed.
     *
     * @param name docker image name
     * @param version docker image version
     */
    public static boolean removeImage(String name, String version) {
        boolean removed = false;
        Image image = getImage(name, version);
        if (isVersion(image, version)) {
            String id = image.getId().substring(image.getId().lastIndexOf(":") + 1);
            DOCKER_CLIENT.removeImageCmd(id).withForce(true).exec();
            removed = true;
        }
        return removed;
    }

    /**
     * Returns an image based on the provided image name and version. If no image is found then a default empty image.
     * is returned
     *
     * @param name docker image name
     * @param version docker image version
     */
    public static Image getImage(String name, String version) {
        Image result = new Image();
        List<Image> images = DOCKER_CLIENT.listImagesCmd().withImageNameFilter(name).exec();
        for (Image image : images) {
            if (isVersion(image, version)) {
                result = image;
                break;
            }
        }
        return result;
    }
}
