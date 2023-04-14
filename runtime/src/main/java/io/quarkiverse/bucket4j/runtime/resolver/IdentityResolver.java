package io.quarkiverse.bucket4j.runtime.resolver;

/**
 * Identity resolver enable rate limit segmentation,
 * one distinct bucket will be created for each key
 */
public interface IdentityResolver {

    String getIdentityKey();
}
