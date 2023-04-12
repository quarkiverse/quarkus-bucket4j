package io.quarkiverse.bucket4j.runtime.resolver;

import jakarta.inject.Singleton;

@Singleton
public class ConstantResolver implements IdentityKeyResolver {
    @Override
    public String getIdentityKey() {
        return "io.quarkiverse.bucket4j.runtime.resolver.CONSTANT";
    }
}
