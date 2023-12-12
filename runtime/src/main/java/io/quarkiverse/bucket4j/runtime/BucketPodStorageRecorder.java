package io.quarkiverse.bucket4j.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    private BucketPod getBucketPod(MethodDescription methodDescription, String key,
            String identityResolverClassName) {
        RateLimiterConfig.Bucket bucketConfig = config.buckets().get(key);
        if (bucketConfig == null) {
            throw new IllegalStateException("missing limits config for " + key);
        }

        ConfigurationBuilder builder = BucketConfiguration.builder();
        for (RateLimiterConfig.Limit limit : bucketConfig.limits()) {
            builder.addLimit(Bandwidth.builder()
                    .capacity(limit.permittedUses())
                    .refillGreedy(limit.permittedUses(), limit.period())
                    .build());
        }
        String id = bucketConfig.shared() ? key : key + methodDescription.hashCode();
        try {
            return new BucketPod(id, builder.build(),
                    (Class<? extends IdentityResolver>) Thread.currentThread().getContextClassLoader()
                            .loadClass(Optional.ofNullable(identityResolverClassName).orElse(bucketConfig.identityResolver())));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }

    }

    public void registerMethod(MethodDescription description,
            String key, String identityResolverClassName) {
        pods.put(description, getBucketPod(description, key, identityResolverClassName));
    }

    public RuntimeValue<BucketPodStorage> create() {
        return new RuntimeValue<>(methodDescription -> pods.get(methodDescription));
    }
}
