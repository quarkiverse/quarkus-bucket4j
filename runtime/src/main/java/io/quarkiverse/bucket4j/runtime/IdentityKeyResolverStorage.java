package io.quarkiverse.bucket4j.runtime;

import java.lang.reflect.Method;

import jakarta.enterprise.inject.spi.CDI;

import io.quarkiverse.bucket4j.runtime.resolver.IdentityKeyResolver;

public interface IdentityKeyResolverStorage {

    default IdentityKeyResolver getIdentityKeyResolver(Method method) {
        return CDI.current().select(getIdentityKeyResolver(MethodDescription.ofMethod(method))).get();
    }

    Class<? extends IdentityKeyResolver> getIdentityKeyResolver(MethodDescription methodDescription);

}
