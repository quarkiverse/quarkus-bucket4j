package io.quarkiverse.bucket4j.runtime;

import java.util.HashMap;
import java.util.Map;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConfigurationBuilder;
import io.quarkiverse.bucket4j.runtime.resolver.IdentityResolver;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class BucketPodStorageRecorder {

    private final RateLimiterConfig config;
    Map<MethodDescription, BucketPod> pods = new HashMap<>();

    public BucketPodStorageRecorder(RateLimiterConfig config) {
        this.config = config;
    }

    private BucketPod getBucketPod(MethodDescription methodDescription, String key, String resolverClassName) {
        RateLimiterConfig.Bucket bucket = config.buckets().get(key);
        if (bucket == null) {
            throw new IllegalStateException("missing limits config for " + key);
        }

        ConfigurationBuilder builder = BucketConfiguration.builder();
        for (RateLimiterConfig.Limit limit : bucket.limits()) {
            builder.addLimit(Bandwidth.simple(limit.permittedUses(), limit.period()));
        }
        String id = bucket.shared() ? key : key + methodDescription.hashCode();
        try {
            return new BucketPod(id, builder.build(),
                    (Class<? extends IdentityResolver>) getClass().getClassLoader().loadClass(resolverClassName));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }

    }

    public void registerMethod(MethodDescription description,
            String key, String resolverClassName) {
        pods.putIfAbsent(description, getBucketPod(description, key, resolverClassName));
    }

    public RuntimeValue<BucketPodStorage> create() {
        return new RuntimeValue<>(methodDescription -> pods.get(methodDescription));
    }
}
