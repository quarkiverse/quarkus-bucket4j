package io.quarkiverse.bucket4j.deployment;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Priorities;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;

import io.quarkiverse.bucket4j.runtime.BucketPodStorage;
import io.quarkiverse.bucket4j.runtime.BucketPodStorageRecorder;
import io.quarkiverse.bucket4j.runtime.DefaultProxyManagerProducer;
import io.quarkiverse.bucket4j.runtime.MethodDescription;
import io.quarkiverse.bucket4j.runtime.RateLimitException;
import io.quarkiverse.bucket4j.runtime.RateLimitExceptionMapper;
import io.quarkiverse.bucket4j.runtime.RateLimited;
import io.quarkiverse.bucket4j.runtime.RateLimitedInterceptor;
import io.quarkiverse.bucket4j.runtime.resolver.ConstantResolver;
import io.quarkiverse.bucket4j.runtime.resolver.IdentityResolver;
import io.quarkiverse.bucket4j.runtime.resolver.IpResolver;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanArchiveIndexBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.resteasy.common.spi.ResteasyJaxrsProviderBuildItem;
import io.quarkus.resteasy.reactive.spi.ExceptionMapperBuildItem;

class Bucket4jProcessor {

    public static final DotName RATE_LIMITED_INTERCEPTOR = DotName.createSimple(RateLimitedInterceptor.class.getName());
    public static final DotName RATE_LIMITED = DotName.createSimple(RateLimited.class.getName());
    public static final DotName IDENTITY_RESOLVER = DotName.createSimple(IdentityResolver.class.getName());

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
    UnremovableBeanBuildItem unremovableIdentityResolvers() {
        return UnremovableBeanBuildItem.beanTypes(IDENTITY_RESOLVER);
    }

    @BuildStep
    void gatherRateLimitCheck(BeanArchiveIndexBuildItem beanArchiveBuildItem,
            BuildProducer<RateLimitCheckBuildItem> producer) {

        Collection<AnnotationInstance> instances = beanArchiveBuildItem.getIndex().getAnnotations(RATE_LIMITED);
        Map<MethodDescription, RateLimitCheckBuildItem> visited = new HashMap<>();

        for (AnnotationInstance instance : instances) {
            AnnotationTarget target = instance.target();
            if (target.kind() == AnnotationTarget.Kind.METHOD) {
                visit(visited, instance, target.asMethod());
            }
        }

        for (AnnotationInstance instance : instances) {
            AnnotationTarget target = instance.target();
            if (target.kind() == AnnotationTarget.Kind.CLASS && !RATE_LIMITED_INTERCEPTOR.equals(target.asClass().name())) {
                List<MethodInfo> methods = target.asClass().methods();
                for (MethodInfo methodInfo : methods) {
                    visit(visited, instance, methodInfo);
                }
            }
        }

        visited.values().forEach(producer::produce);

    }

    private void visit(Map<MethodDescription, RateLimitCheckBuildItem> visited, AnnotationInstance instance,
            MethodInfo methodInfo) {
        visited.computeIfAbsent(createDescription(methodInfo),
                md -> new RateLimitCheckBuildItem(md, instance.value("bucket").asString(), getIdentityResolver(instance)));
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    void createBucketPodStorage(
            BucketPodStorageRecorder recorder,
            List<RateLimitCheckBuildItem> rateLimitChecks,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeans) {

        rateLimitChecks.forEach(
                item -> recorder.registerMethod(item.getMethodDescription(), item.getBucket(), item.getIdentityResolver()));

        syntheticBeans.produce(
                SyntheticBeanBuildItem.configure(BucketPodStorage.class)
                        .scope(ApplicationScoped.class)
                        .unremovable()
                        .runtimeValue(recorder.create())
                        .done());
    }

    private String getIdentityResolver(AnnotationInstance instance) {
        return Optional.ofNullable(instance.value("identityResolver"))
                .map(AnnotationValue::asString)
                .orElse(null);
    }

    private MethodDescription createDescription(MethodInfo method) {
        String[] params = new String[method.parametersCount()];
        for (int i = 0; i < method.parametersCount(); ++i) {
            params[i] = method.parameterType(i).name().toString();
        }
        return new MethodDescription(method.declaringClass().name().toString(), method.name(), params);
    }

}
