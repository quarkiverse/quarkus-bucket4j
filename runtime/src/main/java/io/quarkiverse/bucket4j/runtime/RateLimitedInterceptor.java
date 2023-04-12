package io.quarkiverse.bucket4j.runtime;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import io.github.bucket4j.Bucket;

@RateLimited
@Interceptor
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
public class RateLimitedInterceptor {

    @Inject
    BucketPodStorage bucketPodStorage;

    @AroundInvoke
    Object around(InvocationContext context) throws Throwable {

        Bucket bucket = bucketPodStorage.getBucketPod(context.getMethod()).getBucket("test");

        if (bucket.tryConsume(1)) {
            return context.proceed();
        }
        throw new RateLimitException();
    }
}
