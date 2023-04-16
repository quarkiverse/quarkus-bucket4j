package io.quarkiverse.bucket4j.test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
                    .addAsResource(new StringAsset("quarkus.rate-limiter.buckets.group1.limits[0].permitted-uses: 10\n" +
                            "quarkus.rate-limiter.buckets.group1.limits[0].period: 1S\n" +
                            "quarkus.rate-limiter.buckets.group1.limits[1].permitted-uses: 100\n" +
                            "quarkus.rate-limiter.buckets.group1.limits[1].period: 5M\n" +
                            "quarkus.rate-limiter.buckets.group2.limits[0].permitted-uses: 1\n" +
                            "quarkus.rate-limiter.buckets.group2.limits[0].period: 1S\n"), "application.properties"));

    @Inject
    BucketPodStorage storage;

    @Test
    public void podIsCorrectlyCreatedForAnnotatedMethods() throws NoSuchMethodException {
        BucketPod pod = storage.getBucketPod(RateLimitedMethods.class.getMethod("limited"));
        assertThat(pod).isNotNull();
        assertThat(pod.getId()).isEqualTo("group1");
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
    public void podIsCorrectlyCreatedForAnnotatedClass() throws NoSuchMethodException {
        BucketPod pod = storage.getBucketPod(RateLimitedClass.class.getMethod("limited"));
        assertThat(pod).isNotNull();
        assertThat(pod.getId()).isEqualTo("group1");
        assertThat(pod.getIdentityResolver())
                .isNotNull()
                .isOfAnyClassIn(ConstantResolver.class);
        pod = storage.getBucketPod(RateLimitedClass.class.getMethod("limitedAnnotated"));
        assertThat(pod).isNotNull();
        assertThat(pod.getId()).isEqualTo("group2");
        assertThat(pod.getIdentityResolver())
                .isNotNull()
                .isOfAnyClassIn(ConstantResolver.class);
    }

    @ApplicationScoped
    public static class RateLimitedMethods {

        @RateLimited(bucket = "group1")
        public String limited() {
            return "LIMITED";
        }

    }

    @ApplicationScoped
    @RateLimited(bucket = "group1")
    public static class RateLimitedClass {
        public String limited() {
            return "LIMITED";
        }

        @RateLimited(bucket = "group2")
        public String limitedAnnotated() {
            return "LIMITED";
        }
    }
}
