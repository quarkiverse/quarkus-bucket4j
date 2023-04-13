package io.quarkiverse.bucket4j.runtime.resolver;

import jakarta.inject.Singleton;

/**
 * Using this resolver will disable completely the rate limiting segmentation
 * For a given rate limited method, all requests will use the same bucket
 */
@Singleton
public class ConstantResolver implements IdentityKeyResolver {
    @Override
    public String getIdentityKey() {
        return "io.quarkiverse.bucket4j.runtime.resolver.CONSTANT";
    }
}
