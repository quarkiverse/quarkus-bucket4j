package io.quarkiverse.bucket4j.runtime;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

@RateLimited
@Interceptor
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
public class RateLimitedInterceptor {

    @Inject
    BucketPodStorage bucketPodStorage;

    @ConfigProperty(name = "quarkus.rate-limiter.enabled")
    boolean enabled;

    @AroundInvoke
    Object around(InvocationContext context) throws Throwable {
        if (!enabled) {
            return context.proceed();
        }
        Bucket bucket = bucketPodStorage.getBucketPod(context.getMethod()).getBucket();
        ConsumptionProbe consumptionProbe = bucket.tryConsumeAndReturnRemaining(1);
        if (consumptionProbe.isConsumed()) {
            return context.proceed();
        }
        throw new RateLimitException(consumptionProbe.getNanosToWaitForRefill() / 1000_000L);
    }

}
