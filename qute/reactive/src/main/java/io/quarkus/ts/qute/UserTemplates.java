package io.quarkus.ts.qute;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;

public class UserTemplates {
    @CheckedTemplate(basePath = "")
    public static record UserWithOrder(int id, String name, Long orderNumber, boolean isActive) implements TemplateInstance {
    }

    @CheckedTemplate(basePath = "")
    public static record UserWithOrder$theFragment(String name, int id) implements TemplateInstance {
    }

}
