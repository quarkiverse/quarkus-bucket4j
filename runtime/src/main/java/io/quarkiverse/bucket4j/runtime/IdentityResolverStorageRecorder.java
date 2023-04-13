package io.quarkiverse.bucket4j.runtime;

import java.util.HashMap;
import java.util.Map;

import io.quarkiverse.bucket4j.runtime.resolver.IdentityResolver;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class IdentityResolverStorageRecorder {

    private final RateLimiterConfig config;

    public IdentityResolverStorageRecorder(RateLimiterConfig config) {
        this.config = config;
    }

    Map<MethodDescription, Class<? extends IdentityResolver>> resolvers = new HashMap<>();

    public void registerMethod(MethodDescription description, String className) {

        try {
            resolvers.put(description, (Class<? extends IdentityResolver>) getClass().getClassLoader().loadClass(className));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public RuntimeValue<IdentityResolverStorage> create() {
        return new RuntimeValue<>(methodDescription -> resolvers.get(methodDescription));
    }
}
