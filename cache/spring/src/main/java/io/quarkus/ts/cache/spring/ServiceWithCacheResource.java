package io.quarkus.ts.cache.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/services")
public class ServiceWithCacheResource {

    public static final String APPLICATION_SCOPE_SERVICE_PATH = "application-scope";
    public static final String REQUEST_SCOPE_SERVICE_PATH = "request-scope";

    @Autowired
    ApplicationScopeService applicationScopeService;

    @Autowired
    RequestScopeService requestScopeService;

    @GetMapping("/{service}")
    public String getValueFromService(@PathVariable("service") String service) {
        return lookupServiceByPathParam(service).getValue();
    }

    @PostMapping("/{service}/cache/reset")
    public String resetCacheFromService(@PathVariable("service") String service) {
        return lookupServiceByPathParam(service).resetCache();
    }

    @PostMapping("/{service}/invalidate-cache")
    public void invalidateCacheFromService(@PathVariable("service") String service) {
        lookupServiceByPathParam(service).invalidate();
    }

    @PostMapping("/{service}/invalidate-cache-all")
    public void invalidateCacheAllFromService(@PathVariable("service") String service) {
        lookupServiceByPathParam(service).invalidateAll();
    }

    @GetMapping("/{service}/using-prefix/{prefix}")
    public String getValueUsingPrefixFromService(@PathVariable("service") String service,
            @PathVariable("prefix") String prefix) {
        return lookupServiceByPathParam(service).getValueWithPrefix(prefix);
    }

    @PostMapping("/{service}/using-prefix/{prefix}/invalidate-cache")
    public void invalidateCacheUsingPrefixFromService(@PathVariable("service") String service,
            @PathVariable("prefix") String prefix) {
        lookupServiceByPathParam(service).invalidateWithPrefix(prefix);
    }

    @PostMapping("/{service}/using-prefix/{prefix}/cache/reset")
    public String resetCacheUsingPrefixFromService(@PathVariable("service") String service,
            @PathVariable("prefix") String prefix) {
        return lookupServiceByPathParam(service).resetCacheWithPrefix(prefix);
    }

    private BaseServiceWithCache lookupServiceByPathParam(String service) {
        if (APPLICATION_SCOPE_SERVICE_PATH.equals(service)) {
            return applicationScopeService;
        } else if (REQUEST_SCOPE_SERVICE_PATH.equals(service)) {
            return requestScopeService;
        }

        throw new IllegalArgumentException("Service " + service + " is not recognised");
    }
}
