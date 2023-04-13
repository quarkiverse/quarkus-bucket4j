package io.quarkiverse.bucket4j.runtime;

import io.github.bucket4j.BucketConfiguration;

public class BucketPod {

    private final String id;

    public BucketPod(String id, BucketConfiguration configuration) {
        this.id = id;
        this.configuration = configuration;
    }

    final BucketConfiguration configuration;

    public String getId() {
        return this.id;
    }

    public BucketConfiguration getConfiguration() {
        return this.configuration;
    }

}
