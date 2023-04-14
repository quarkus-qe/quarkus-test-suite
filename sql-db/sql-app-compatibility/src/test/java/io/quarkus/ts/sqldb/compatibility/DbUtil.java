package io.quarkus.ts.sqldb.compatibility;

import java.util.HashMap;
import java.util.Map;

public class DbUtil {

    private static final String COMMA = ":";
    private static final Map<String, String[]> PROPERTY_TO_IMAGE = new HashMap<>();

    private DbUtil() {
        // util class
    }

    public static String getImageVersion(String imageProperty) {
        return getImage(imageProperty)[1];
    }

    public static String getImageName(String imageProperty) {
        return getImage(imageProperty)[0];
    }

    private static String[] getImage(String imageProperty) {
        return PROPERTY_TO_IMAGE.computeIfAbsent(imageProperty, ip -> {
            final String image = System.getProperty(imageProperty);
            if (image == null) {
                throw new IllegalStateException(String.format("System property '%s' is missing.", imageProperty));
            }
            if (!image.contains(COMMA)) {
                throw new IllegalStateException(String.format("'%s' is not valid Docker image", image));
            }
            return image.split(COMMA);
        });
    }

}
