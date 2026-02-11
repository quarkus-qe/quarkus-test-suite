package io.quarkus.ts.cache.spring.services;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

@Service
@RequestScope
public class RequestScopeService extends BaseServiceWithCache {
}
