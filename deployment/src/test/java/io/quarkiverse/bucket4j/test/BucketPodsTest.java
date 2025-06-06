package io.quarkiverse.bucket4j.test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.github.bucket4j.BucketConfiguration;
import io.quarkiverse.bucket4j.runtime.BucketPod;
import io.quarkiverse.bucket4j.runtime.BucketPodStorage;
import io.quarkiverse.bucket4j.runtime.RateLimited;
import io.quarkiverse.bucket4j.runtime.resolver.ConstantResolver;
import io.quarkus.test.QuarkusUnitTest;

public class BucketPodsTest {

    // Start unit test with your extension loaded
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(RateLimitedMethods.class)
                    .addClass(RateLimitedClass.class)
                    .addAsResource(new StringAsset(
                            "quarkus.rate-limiter.buckets.annotated-method.shared: true\n" +
                                    "quarkus.rate-limiter.buckets.annotated-method.limits[0].permitted-uses: 10\n" +
                                    "quarkus.rate-limiter.buckets.annotated-method.limits[0].period: 1S\n" +
                                    "quarkus.rate-limiter.buckets.annotated-method.limits[1].permitted-uses: 100\n" +
                                    "quarkus.rate-limiter.buckets.annotated-method.limits[1].period: 5M\n" +
                                    "quarkus.rate-limiter.buckets.annotated-class.shared: true\n" +
                                    "quarkus.rate-limiter.buckets.annotated-class.limits[0].permitted-uses: 1\n" +
                                    "quarkus.rate-limiter.buckets.annotated-class.limits[0].period: 1S\n" +
                                    "quarkus.rate-limiter.buckets.isolated-method.shared: false\n" +
                                    "quarkus.rate-limiter.buckets.isolated-method.limits[0].permitted-uses: 1\n" +
                                    "quarkus.rate-limiter.buckets.isolated-method.limits[0].period: 1S\n"),
                            "application.properties"));

    @Inject
    BucketPodStorage storage;

    @Test
    public void podIsCorrectlyCreatedForAnnotatedMethods() throws NoSuchMethodException {
        List<BucketPod> pods = storage.getBucketPods(RateLimitedMethods.class.getMethod("limited"));
        assertEquals(1, pods.size());
        BucketPod pod = pods.get(0);
        assertThat(pod).isNotNull();
        assertThat(pod.getId()).isEqualTo("annotated-method");
        BucketConfiguration configuration = pod.getConfiguration();
        assertThat(configuration.getBandwidths())
                .hasSize(2);
        assertThat(configuration.getBandwidths()[0].getCapacity())
                .isEqualTo(10L);
        assertThat(configuration.getBandwidths()[0].getRefillPeriodNanos())
                .isEqualTo(1000_000_000L);
        assertThat(configuration.getBandwidths()[1].getCapacity())
                .isEqualTo(100L);
        assertThat(configuration.getBandwidths()[1].getRefillPeriodNanos())
                .isEqualTo(300_000_000_000L);
        assertThat(pod.getIdentityResolver())
                .isNotNull()
                .isOfAnyClassIn(ConstantResolver.class);
    }

    @Test
    public void podIsCorrectlyCreatedForIsolatedMethods() throws NoSuchMethodException {
        List<BucketPod> pods = storage.getBucketPods(RateLimitedMethods.class.getMethod("isolatedMethod"));
        assertEquals(1, pods.size());
        BucketPod pod = pods.get(0);
        assertThat(pod).isNotNull();
        assertThat(pod.getId()).isEqualTo("isolated-method-1867211146");
    }

    @Test
    public void podIsCorrectlyCreatedForAnnotatedClass() throws NoSuchMethodException {
        List<BucketPod> pods = storage.getBucketPods(RateLimitedClass.class.getMethod("limited"));
        assertEquals(1, pods.size());
        BucketPod pod = pods.get(0);
        assertThat(pod).isNotNull();
        assertThat(pod.getId()).isEqualTo("annotated-class");
        assertThat(pod.getIdentityResolver())
                .isNotNull()
                .isOfAnyClassIn(ConstantResolver.class);
        pods = storage.getBucketPods(RateLimitedClass.class.getMethod("limitedAnnotated"));
        assertEquals(1, pods.size());
        pod = pods.get(0);
        assertThat(pod).isNotNull();
        assertThat(pod.getId()).isEqualTo("annotated-method");
        assertThat(pod.getIdentityResolver())
                .isNotNull()
                .isOfAnyClassIn(ConstantResolver.class);
    }

    @ApplicationScoped
    public static class RateLimitedMethods {

        @RateLimited(bucket = "annotated-method")
        public String limited() {
            return "LIMITED";
        }

        @RateLimited(bucket = "isolated-method")
        public String isolatedMethod() {
            return "LIMITED";
        }

    }

    @ApplicationScoped
    @RateLimited(bucket = "annotated-class")
    public static class RateLimitedClass {
        public String limited() {
            return "LIMITED";
        }

        @RateLimited(bucket = "annotated-method")
        public String limitedAnnotated() {
            return "LIMITED";
        }
    }
}
