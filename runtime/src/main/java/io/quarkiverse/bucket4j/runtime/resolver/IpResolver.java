package io.quarkiverse.bucket4j.runtime.resolver;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import io.vertx.ext.web.RoutingContext;

@RequestScoped
public class IpResolver implements IdentityKeyResolver {
    @Inject
    RoutingContext context;

    @Override
    public String getIdentityKey() {
        return context.request().host();
    }
}
