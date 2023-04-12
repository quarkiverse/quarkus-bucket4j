package io.quarkiverse.bucket4j.runtime;

import java.lang.reflect.Method;

public interface BucketPodStorage {

    default BucketPod getBucketPod(Method method) {
        return getBucketPod(MethodDescription.ofMethod(method));
    }

    BucketPod getBucketPod(MethodDescription methodDescription);

}
