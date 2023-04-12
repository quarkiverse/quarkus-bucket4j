package io.quarkiverse.bucket4j.runtime;

import java.util.HashMap;
import java.util.Map;

import io.quarkiverse.bucket4j.runtime.resolver.IdentityKeyResolver;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class IdentityKeyResolverStorageRecorder {

    private final RateLimiterConfig config;

    public IdentityKeyResolverStorageRecorder(RateLimiterConfig config) {
        this.config = config;
    }

    Map<MethodDescription, Class<? extends IdentityKeyResolver>> resolvers = new HashMap<>();

    public void registerMethod(MethodDescription description, String className) {

        try {
            resolvers.put(description, (Class<? extends IdentityKeyResolver>) getClass().getClassLoader().loadClass(className));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public RuntimeValue<IdentityKeyResolverStorage> create() {
        return new RuntimeValue<>(methodDescription -> resolvers.get(methodDescription));
    }
}
