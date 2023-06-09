package io.quarkiverse.bucket4j.runtime;

import jakarta.enterprise.inject.spi.CDI;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.quarkiverse.bucket4j.runtime.resolver.IdentityResolver;

public class BucketPod {

    private final String id;
    private final Class<? extends IdentityResolver> identityResolver;

    public BucketPod(String id, BucketConfiguration configuration, Class<? extends IdentityResolver> identityResolver) {
        this.id = id;
        this.configuration = configuration;
        this.identityResolver = identityResolver;
    }

    final BucketConfiguration configuration;

    public String getId() {
        return this.id;
    }

    public BucketConfiguration getConfiguration() {
        return this.configuration;
    }

    public IdentityResolver getIdentityResolver() {
        return CDI.current().select(identityResolver).get();
    }

    public Bucket getBucket(ProxyManager<String> proxyManager) {
        return proxyManager.builder().build(getId() + "_" + getIdentityResolver().getIdentityKey(), getConfiguration());
    }
}
