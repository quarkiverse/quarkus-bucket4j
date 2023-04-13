package io.quarkiverse.bucket4j.runtime;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
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

        ConsumptionProbe consumptionProbe = bucket.tryConsumeAndReturnRemaining(1);
        if (consumptionProbe.isConsumed()) {
            return context.proceed();
        }
        throw new RateLimitException(consumptionProbe.getNanosToWaitForRefill());
    }

    private Bucket getBucket(BucketPod pod, IdentityKeyResolver keyResolver) {
        return proxyManager.builder().build(pod.getId() + "_" + keyResolver.getIdentityKey(), pod.getConfiguration());
    }
}
