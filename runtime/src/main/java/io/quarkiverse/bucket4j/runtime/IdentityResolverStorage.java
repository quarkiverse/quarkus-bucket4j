package io.quarkiverse.bucket4j.runtime;

import java.lang.reflect.Method;

import jakarta.enterprise.inject.spi.CDI;

import io.quarkiverse.bucket4j.runtime.resolver.IdentityResolver;

public interface IdentityResolverStorage {

    default IdentityResolver getIdentityKeyResolver(Method method) {
        return CDI.current().select(getIdentityKeyResolver(MethodDescription.ofMethod(method))).get();
    }

    Class<? extends IdentityResolver> getIdentityKeyResolver(MethodDescription methodDescription);

}
