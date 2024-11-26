package io.quarkus.ts.http.undertow;

import static io.quarkus.ts.http.undertow.UndertowMissingServletClassIT.replaceForInvalidXML;

import io.quarkus.test.services.quarkus.ProdQuarkusApplicationManagedResourceBuilder;

public class WithMissingServlet extends ProdQuarkusApplicationManagedResourceBuilder {

    @Override
    protected void copyResourcesToAppFolder() {
        super.copyResourcesToAppFolder();
        replaceForInvalidXML(getContext().getOwner());
    }
}
