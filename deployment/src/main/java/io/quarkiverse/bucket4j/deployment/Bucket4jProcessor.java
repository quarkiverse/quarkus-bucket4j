package io.quarkiverse.bucket4j.deployment;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Priorities;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;

import io.quarkiverse.bucket4j.runtime.BucketPod;
import io.quarkiverse.bucket4j.runtime.BucketPodStorage;
import io.quarkiverse.bucket4j.runtime.BucketPodStorageRecorder;
import io.quarkiverse.bucket4j.runtime.DefaultProxyManagerProducer;
import io.quarkiverse.bucket4j.runtime.IdentityResolverStorage;
import io.quarkiverse.bucket4j.runtime.IdentityResolverStorageRecorder;
import io.quarkiverse.bucket4j.runtime.MethodDescription;
import io.quarkiverse.bucket4j.runtime.RateLimitException;
import io.quarkiverse.bucket4j.runtime.RateLimitExceptionMapper;
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
import io.quarkus.resteasy.common.spi.ResteasyJaxrsProviderBuildItem;
import io.quarkus.resteasy.reactive.spi.ExceptionMapperBuildItem;
import io.quarkus.runtime.RuntimeValue;

class Bucket4jProcessor {

    public static final DotName RATE_LIMITED_INTERCEPTOR = DotName.createSimple(RateLimitedInterceptor.class.getName());

    public static final DotName RATE_LIMITED = DotName.createSimple(RateLimited.class.getName());

    private static final String FEATURE = "bucket4j";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem caffeineProxyManager() {
        return AdditionalBeanBuildItem.unremovableOf(DefaultProxyManagerProducer.class);
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
    void exceptionMapper(BuildProducer<ResteasyJaxrsProviderBuildItem> resteasyJaxrsProviderBuildItemBuildProducer,
                         BuildProducer<ExceptionMapperBuildItem> exceptionMapperBuildItemBuildProducer) {

        resteasyJaxrsProviderBuildItemBuildProducer
                .produce(new ResteasyJaxrsProviderBuildItem(RateLimitExceptionMapper.class.getName()));
        exceptionMapperBuildItemBuildProducer
                .produce(new ExceptionMapperBuildItem(RateLimitExceptionMapper.class.getName(),
                        RateLimitException.class.getName(), Priorities.USER + 100, false));

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

        for (AnnotationInstance instance : instances) {
            AnnotationTarget target = instance.target();
            if (target.kind() == AnnotationTarget.Kind.METHOD) {
                MethodInfo methodInfo = target.asMethod();
                recorder.registerMethod(createDescription(methodInfo),
                        instance.value("bucket").asString());
            }
        }

        for (AnnotationInstance instance : instances) {
            AnnotationTarget target = instance.target();
            if (target.kind() == AnnotationTarget.Kind.CLASS && !RATE_LIMITED_INTERCEPTOR.equals(target.asClass().name())) {
                List<MethodInfo> methods = target.asClass().methods();
                for (MethodInfo methodInfo : methods) {
                    recorder.registerMethod(createDescription(methodInfo),
                            instance.value("bucket").asString());
                }
            }
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
                                    IdentityResolverStorageRecorder recorder) {

        Collection<AnnotationInstance> instances = beanArchiveBuildItem.getIndex().getAnnotations(RATE_LIMITED);

        for (AnnotationInstance instance : instances) {

            AnnotationTarget target = instance.target();
            if (target.kind() == AnnotationTarget.Kind.METHOD) {
                MethodInfo methodInfo = target.asMethod();
                String identityResolver = instance
                        .valueWithDefault(beanArchiveBuildItem.getIndex(), "identityResolver").asClass().name().toString();
                recorder.registerMethod(createDescription(methodInfo), identityResolver);
            }
        }

        for (AnnotationInstance instance : instances) {
            AnnotationTarget target = instance.target();
            if (target.kind() == AnnotationTarget.Kind.CLASS && !RATE_LIMITED_INTERCEPTOR.equals(target.asClass().name())) {
                String identityResolver = instance
                        .valueWithDefault(beanArchiveBuildItem.getIndex(), "identityResolver").asClass().name().toString();
                List<MethodInfo> methods = target.asClass().methods();
                for (MethodInfo methodInfo : methods) {
                    recorder.registerMethod(createDescription(methodInfo), identityResolver);
                }
            }
        }

        syntheticBeans.produce(
                SyntheticBeanBuildItem.configure(IdentityResolverStorage.class)
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
