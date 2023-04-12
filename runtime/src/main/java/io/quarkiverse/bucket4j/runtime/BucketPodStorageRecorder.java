package io.quarkiverse.bucket4j.runtime;

import java.util.HashMap;
import java.util.Map;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConfigurationBuilder;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class BucketPodStorageRecorder {

    private final RateLimiterConfig config;

    public BucketPodStorageRecorder(RateLimiterConfig config) {
        this.config = config;
    }

    Map<MethodDescription, BucketPod> pods = new HashMap<>();

    public RuntimeValue<BucketPod> getBucketPod(String key) {
        ConfigurationBuilder builder = BucketConfiguration.builder();
        for (RateLimiterConfig.Limit limit : config.limits().get(key)) {
            builder.addLimit(Bandwidth.simple(limit.maxUsage(), limit.period()));
        }
        return new RuntimeValue<>(new BucketPod(builder.build()));
    }

    public void registerMethod(MethodDescription description,
            RuntimeValue<BucketPod> bucketPod) {
        pods.put(description, bucketPod.getValue());
    }

    public RuntimeValue<BucketPodStorage> create() {
        return new RuntimeValue<>(methodDescription -> pods.get(methodDescription));
    }
}
