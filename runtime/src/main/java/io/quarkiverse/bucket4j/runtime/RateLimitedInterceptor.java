package io.quarkiverse.bucket4j.runtime;

import java.util.List;

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
        List<BucketPod> bucketPods = bucketPodStorage.getBucketPods(context.getMethod());

        // Consume a token on all matching bucket and keep the highest wait time
        long nanoWaitTime = 0;
        for (BucketPod bucketPod : bucketPods) {
            long bucketNanoWaitTime = bucketPod.consumeAndReturnNanoWaitTime();
            if (bucketNanoWaitTime > nanoWaitTime) {
                nanoWaitTime = bucketNanoWaitTime;
            }
        }

        if (nanoWaitTime == 0) {
            return context.proceed();
        }
        throw new RateLimitException(nanoWaitTime / 1000_000L);
    }

}
