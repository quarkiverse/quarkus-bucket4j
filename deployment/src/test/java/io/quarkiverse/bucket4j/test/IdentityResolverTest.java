package io.quarkiverse.bucket4j.test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.bucket4j.runtime.BucketPod;
import io.quarkiverse.bucket4j.runtime.BucketPodStorage;
import io.quarkiverse.bucket4j.runtime.RateLimited;
import io.quarkiverse.bucket4j.runtime.resolver.IdentityResolver;
import io.quarkus.arc.Unremovable;
import io.quarkus.test.QuarkusUnitTest;

public class IdentityResolverTest {

    // Start unit test with your extension loaded
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(TestIdentityResolver.class)
                    .addClass(RateLimitedMethods.class)
                    .addAsResource(new StringAsset(
                            "quarkus.rate-limiter.buckets.by-annotation.limits[0].permitted-uses: 10\n" +
                                    "quarkus.rate-limiter.buckets.by-annotation.limits[0].period: 1S\n" +
                                    "quarkus.rate-limiter.buckets.by-config.identity-resolver: io.quarkiverse.bucket4j.test.IdentityResolverTest$TestIdentityResolver\n"
                                    +
                                    "quarkus.rate-limiter.buckets.by-config.limits[0].permitted-uses: 10\n" +
                                    "quarkus.rate-limiter.buckets.by-config.limits[0].period: 1S\n"),
                            "application.properties"));

    @Inject
    BucketPodStorage storage;

    @Test
    public void identityResolverCanBeConfiguredViaAnnotation() throws NoSuchMethodException {
        BucketPod pod = storage.getBucketPod(RateLimitedMethods.class.getMethod("byAnnotation"));
        assertThat(pod).isNotNull();
        assertThat(pod.getIdentityResolver())
                .isNotNull()
                .isOfAnyClassIn(TestIdentityResolver.class);
    }

    @Test
    public void identityResolverCanBeConfiguredViaConfig() throws NoSuchMethodException {
        BucketPod pod = storage.getBucketPod(RateLimitedMethods.class.getMethod("byConfig"));
        assertThat(pod).isNotNull();
        assertThat(pod.getIdentityResolver())
                .isNotNull()
                .isOfAnyClassIn(TestIdentityResolver.class);
    }

    @Singleton
    @Unremovable
    public static class TestIdentityResolver implements IdentityResolver {

        @Override
        public String getIdentityKey() {
            return "TEST";
        }
    }

    @ApplicationScoped
    public static class RateLimitedMethods {

        @RateLimited(bucket = "by-annotation", identityResolver = TestIdentityResolver.class)
        public String byAnnotation() {
            return "LIMITED";
        }

        @RateLimited(bucket = "by-config")
        public String byConfig() {
            return "LIMITED";
        }
    }

}
