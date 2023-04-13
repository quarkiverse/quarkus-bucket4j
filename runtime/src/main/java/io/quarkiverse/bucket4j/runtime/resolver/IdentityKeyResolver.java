package io.quarkiverse.bucket4j.runtime.resolver;

/**
 * identity resolver enable rate limit segmentation,
 * one distinct bucket will be created for each key
 */
public interface IdentityKeyResolver {

    String getIdentityKey();
}
