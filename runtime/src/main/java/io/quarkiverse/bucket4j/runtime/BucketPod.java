package io.quarkiverse.bucket4j.runtime;

import static io.github.bucket4j.MathType.INTEGER_64_BITS;
import static io.github.bucket4j.TimeMeter.SYSTEM_MILLISECONDS;

import java.util.HashMap;
import java.util.Map;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.local.LockFreeBucket;

public class BucketPod {

    public BucketPod(BucketConfiguration configuration) {
        this.configuration = configuration;
    }

    final BucketConfiguration configuration;
    final Map<String, Bucket> buckets = new HashMap<>();

    public Bucket getBucket(String key) {
        return buckets.computeIfAbsent(key, (k) -> new LockFreeBucket(configuration, INTEGER_64_BITS, SYSTEM_MILLISECONDS));
    }

}
