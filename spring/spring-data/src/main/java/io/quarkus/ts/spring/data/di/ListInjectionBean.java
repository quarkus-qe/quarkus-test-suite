package io.quarkus.ts.spring.data.di;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.springframework.beans.factory.annotation.Autowired;

import io.quarkus.arc.All;
import io.quarkus.arc.properties.IfBuildProperty;

@ApplicationScoped
// Bean causes build error on unpatched Quarkus => enable only on demand to avoid interference with other tests.
@IfBuildProperty(name = ListInjectionBean.LIST_INJECTION_BEAN_ENABLED, stringValue = ListInjectionBean.LIST_INJECTION_BEAN_ENABLED_VALUE)
public class ListInjectionBean {
    public static final String LIST_INJECTION_BEAN_ENABLED = "io.quarkus.ts.spring.data.di.ListInjectionBean.enabled";
    public static final String LIST_INJECTION_BEAN_ENABLED_VALUE = "true";

    List<Service> constructorInjectServices;

    @Autowired
    @All
    List<Service> fieldInjectServices;

    ListInjectionBean(@All List<Service> constructorInjectServices) {
        this.constructorInjectServices = constructorInjectServices;
    }

    interface Service {
    }
}
