package io.quarkus.ts.http.undertow;

import static io.quarkus.ts.http.undertow.UndertowMissingServletClassIT.replaceForInvalidXML;

import java.lang.annotation.Annotation;

import io.quarkus.test.services.quarkus.ProdQuarkusApplicationManagedResourceBuilder;
import io.quarkus.test.utils.ClassPathUtils;

public class WithMissingServlet extends ProdQuarkusApplicationManagedResourceBuilder {

    @Override
    protected void copyResourcesToAppFolder() {
        super.copyResourcesToAppFolder();
        replaceForInvalidXML(getContext().getOwner());
    }

    /**
     * It was suggested here: https://github.com/quarkus-qe/quarkus-test-suite/pull/2218#issuecomment-2504972950,
     * and slightly different from main because there it's used TF version 1.6.z and for 3.15 we use 1.5.z
     */
    @Override
    public void init(Annotation annotation) {
        super.init(annotation);
        initAppClasses(ClassPathUtils.findAllClassesFromSource());
    }
}
