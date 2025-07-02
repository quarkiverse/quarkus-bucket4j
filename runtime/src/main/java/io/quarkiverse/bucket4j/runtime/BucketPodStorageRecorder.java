package io.quarkiverse.bucket4j.runtime;

import static io.quarkiverse.bucket4j.runtime.RateLimiterRuntimeConfig.Limit.RefillSpeed.GREEDY;
import static io.quarkiverse.bucket4j.runtime.RateLimiterRuntimeConfig.Limit.RefillSpeed.INTERVAL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BandwidthBuilder;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConfigurationBuilder;
import io.quarkiverse.bucket4j.runtime.RateLimiterRuntimeConfig.Bucket;
import io.quarkiverse.bucket4j.runtime.RateLimiterRuntimeConfig.Limit;
import io.quarkiverse.bucket4j.runtime.resolver.IdentityResolver;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class BucketPodStorageRecorder {

    private final RateLimiterRuntimeConfig config;
    Map<MethodDescription, List<BucketPod>> pods = new HashMap<>();

    public BucketPodStorageRecorder(RateLimiterRuntimeConfig config) {
        this.config = config;
    }

    private BucketPod getBucketPod(MethodDescription methodDescription, String key,
            String identityResolverClassName) {
        Bucket bucketConfig = config.buckets().get(key);
        if (bucketConfig == null) {
            throw new IllegalStateException("missing limits config for " + key);
        }
        ConfigurationBuilder builder = BucketConfiguration.builder();
        for (Limit limit : bucketConfig.limits()) {
            builder.addLimit(createBandwidth(limit));
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

    private Bandwidth createBandwidth(Limit limit) {
        BandwidthBuilder.BandwidthBuilderRefillStage refillStage = Bandwidth.builder()
                .capacity(limit.permittedUses());
        BandwidthBuilder.BandwidthBuilderBuildStage stageBuilder;
        Limit.RefillSpeed refillSpeed = limit.refillSpeed();
        if (refillSpeed == GREEDY) {
            stageBuilder = refillStage
                    .refillGreedy(limit.permittedUses(), limit.period());
        } else if (refillSpeed == INTERVAL) {
            stageBuilder = refillStage
                    .refillIntervally(limit.permittedUses(), limit.period());
        } else {
            throw new IllegalArgumentException();
        }
        if (limit.initialTokens().isPresent()) {
            stageBuilder = stageBuilder.initialTokens(limit.initialTokens().get());
        }
        return stageBuilder.build();
    }

    public void registerMethod(MethodDescription description,
            String key, String identityResolverClassName) {
        List<BucketPod> bucketPods = pods.computeIfAbsent(description, k -> new ArrayList<>());
        bucketPods.add(getBucketPod(description, key, identityResolverClassName));
    }

    public BucketPodStorage create() {
        return methodDescription -> pods.get(methodDescription);
    }
}
