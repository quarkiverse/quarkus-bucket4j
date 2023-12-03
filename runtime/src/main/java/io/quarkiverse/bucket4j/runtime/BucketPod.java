package io.quarkiverse.bucket4j.runtime;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.util.TypeLiteral;

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

    public long consumeAndReturnNanoWaitTime() {
        return getBucket().tryConsumeAndReturnRemaining(1).getNanosToWaitForRefill();
    }

    public BucketConfiguration getConfiguration() {
        return this.configuration;
    }

    public IdentityResolver getIdentityResolver() {
        return CDI.current().select(identityResolver).get();
    }

    ProxyManager<String> getProxyManager() {
        return CDI.current().select(new TypeLiteral<ProxyManager<String>>() {
        }).get();
    }

    Bucket getBucket() {

        return getProxyManager().builder().build(getId() + "_" + getIdentityResolver().getIdentityKey(),
                this::getConfiguration);
    }
}
