package io.quarkiverse.bucket4j.runtime;

import java.lang.reflect.Method;
import java.util.List;

public interface BucketPodStorage {

    default List<BucketPod> getBucketPods(Method method) {
        return getBucketPods(MethodDescription.ofMethod(method));
    }

    List<BucketPod> getBucketPods(MethodDescription methodDescription);

}
