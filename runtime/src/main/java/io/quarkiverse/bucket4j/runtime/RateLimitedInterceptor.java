package io.quarkiverse.bucket4j.runtime;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.quarkiverse.bucket4j.runtime.resolver.IdentityKeyResolver;

@RateLimited
@Interceptor
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
public class RateLimitedInterceptor {

    @Inject
    BucketPodStorage bucketPodStorage;

    @Inject
    IdentityKeyResolverStorage identityKeyResolverStorage;

    @Inject
    ProxyManager<String> proxyManager;

    @AroundInvoke
    Object around(InvocationContext context) throws Throwable {
        Bucket bucket = getBucket(bucketPodStorage.getBucketPod(context.getMethod()),
                identityKeyResolverStorage.getIdentityKeyResolver(context.getMethod()));

        if (bucket.tryConsume(1)) {
            return context.proceed();
        }
        throw new RateLimitException(bucket.estimateAbilityToConsume(1).getNanosToWaitForRefill());
    }

    private Bucket getBucket(BucketPod pod, IdentityKeyResolver keyResolver) {
        return proxyManager.builder().build(pod.getId() + "_" + keyResolver.getIdentityKey(), pod.getConfiguration());
    }
}
