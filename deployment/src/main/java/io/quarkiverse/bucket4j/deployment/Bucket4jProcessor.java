package io.quarkiverse.bucket4j.deployment;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;

import io.quarkiverse.bucket4j.runtime.BucketPod;
import io.quarkiverse.bucket4j.runtime.BucketPodStorage;
import io.quarkiverse.bucket4j.runtime.BucketPodStorageRecorder;
import io.quarkiverse.bucket4j.runtime.IdentityKeyResolverStorage;
import io.quarkiverse.bucket4j.runtime.IdentityKeyResolverStorageRecorder;
import io.quarkiverse.bucket4j.runtime.MethodDescription;
import io.quarkiverse.bucket4j.runtime.RateLimited;
import io.quarkiverse.bucket4j.runtime.RateLimitedInterceptor;
import io.quarkiverse.bucket4j.runtime.resolver.ConstantResolver;
import io.quarkiverse.bucket4j.runtime.resolver.IpResolver;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanArchiveIndexBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.runtime.RuntimeValue;

class Bucket4jProcessor {

    public static final DotName RATE_LIMITED = DotName.createSimple(RateLimited.class.getName());

    private static final String FEATURE = "bucket4j";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem interceptorBinding() {
        return AdditionalBeanBuildItem.unremovableOf(RateLimitedInterceptor.class);
    }

    @BuildStep
    AdditionalBeanBuildItem constantResolver() {
        return AdditionalBeanBuildItem.unremovableOf(ConstantResolver.class);
    }

    @BuildStep
    void ipResolver(Capabilities capabilities, BuildProducer<AdditionalBeanBuildItem> additionalBeans) {
        if (capabilities.isPresent(Capability.VERTX_HTTP)) {
            additionalBeans.produce(AdditionalBeanBuildItem.unremovableOf(IpResolver.class));
        }
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    void gatherRateLimitCheck(BeanArchiveIndexBuildItem beanArchiveBuildItem,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeans,
            BucketPodStorageRecorder recorder) {

        Collection<AnnotationInstance> instances = beanArchiveBuildItem.getIndex().getAnnotations(RATE_LIMITED);
        //Map<MethodInfo, AnnotationInstance> methodToInstanceCollector = new HashMap<>();
        Map<String, RuntimeValue<BucketPod>> sharedPods = new HashMap<>();
        Map<MethodInfo, RuntimeValue<BucketPod>> perMethodPods = new HashMap<>();

        for (AnnotationInstance instance : instances) {
            AnnotationTarget target = instance.target();
            if (target.kind() == AnnotationTarget.Kind.METHOD) {
                MethodInfo methodInfo = target.asMethod();
                //methodToInstanceCollector.put(methodInfo, instance);
                perMethodPods.put(methodInfo, sharedPods.computeIfAbsent(instance.value("limitsKey").asString(),
                        (key) -> recorder.getBucketPod(key)));
            }
        }

        for (Map.Entry<MethodInfo, RuntimeValue<BucketPod>> methodEntry : perMethodPods
                .entrySet()) {

            recorder.registerMethod(createDescription(methodEntry.getKey()),
                    methodEntry.getValue());
        }

        syntheticBeans.produce(
                SyntheticBeanBuildItem.configure(BucketPodStorage.class)
                        .scope(ApplicationScoped.class)
                        .unremovable()
                        .runtimeValue(recorder.create())
                        .done());
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    void gatherIdentityKeyResolvers(BeanArchiveIndexBuildItem beanArchiveBuildItem,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeans,
            IdentityKeyResolverStorageRecorder recorder) {

        Collection<AnnotationInstance> instances = beanArchiveBuildItem.getIndex().getAnnotations(RATE_LIMITED);

        for (AnnotationInstance instance : instances) {

            AnnotationTarget target = instance.target();
            if (target.kind() == AnnotationTarget.Kind.METHOD) {
                MethodInfo methodInfo = target.asMethod();
                recorder.registerMethod(createDescription(methodInfo), instance
                        .valueWithDefault(beanArchiveBuildItem.getIndex(), "identityResolver").asClass().name().toString());
            }
        }

        syntheticBeans.produce(
                SyntheticBeanBuildItem.configure(IdentityKeyResolverStorage.class)
                        .scope(ApplicationScoped.class)
                        .unremovable()
                        .runtimeValue(recorder.create())
                        .done());
    }

    private MethodDescription createDescription(MethodInfo method) {
        String[] params = new String[method.parametersCount()];
        for (int i = 0; i < method.parametersCount(); ++i) {
            params[i] = method.parameterType(i).name().toString();
        }
        return new MethodDescription(method.declaringClass().name().toString(), method.name(), params);
    }

}
