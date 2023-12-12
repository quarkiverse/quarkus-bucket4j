package io.quarkiverse.bucket4j.deployment;

import io.quarkiverse.bucket4j.runtime.MethodDescription;
import io.quarkus.builder.item.MultiBuildItem;

public final class RateLimitCheckBuildItem extends MultiBuildItem {

    private final MethodDescription methodDescription;
    private final String bucket;
    private final String identityResolver;

    public RateLimitCheckBuildItem(MethodDescription methodDescription, String bucket, String identityResolver) {
        this.methodDescription = methodDescription;
        this.bucket = bucket;
        this.identityResolver = identityResolver;
    }

    public MethodDescription getMethodDescription() {
        return methodDescription;
    }

    public String getBucket() {
        return bucket;
    }

    public String getIdentityResolver() {
        return identityResolver;
    }
}
