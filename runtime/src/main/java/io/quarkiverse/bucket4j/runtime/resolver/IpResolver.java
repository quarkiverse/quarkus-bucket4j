package io.quarkiverse.bucket4j.runtime.resolver;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import io.vertx.ext.web.RoutingContext;

/**
 * Using this resolver will enable rate limit segmentation by IP
 * For a given endpoint, each IP will have its own bucket
 * Vertx Http must be present in the project in order to use this
 */
@RequestScoped
public class IpResolver implements IdentityKeyResolver {
    @Inject
    RoutingContext context;

    @Override
    public String getIdentityKey() {
        return context.request().remoteAddress().host();
    }
}
