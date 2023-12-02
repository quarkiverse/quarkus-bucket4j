package io.quarkiverse.bucket4j.runtime;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import org.eclipse.microprofile.config.inject.ConfigProperty;

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
        long nanoWaitTime = bucketPodStorage.getBucketPod(context.getMethod()).consumeAndReturnNanoWaitTime();
        if (nanoWaitTime == 0) {
            return context.proceed();
        }
        throw new RateLimitException(nanoWaitTime / 1000_000L);
    }

}
