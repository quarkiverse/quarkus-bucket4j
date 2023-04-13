package io.quarkiverse.bucket4j.test;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.github.bucket4j.BucketConfiguration;
import io.quarkiverse.bucket4j.runtime.BucketPod;
import io.quarkiverse.bucket4j.runtime.BucketPodStorage;
import io.quarkiverse.bucket4j.runtime.RateLimited;
import io.quarkus.test.QuarkusUnitTest;

public class BucketPodsTest {

    // Start unit test with your extension loaded
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(RateLimitedMethods.class)
                    .addAsResource(new StringAsset("quarkus.rate-limiter.limits.group1[0].max-usage: 10\n" +
                            "quarkus.rate-limiter.limits.group1[0].period: 1S\n" +
                            "quarkus.rate-limiter.limits.group1[1].max-usage: 100\n" +
                            "quarkus.rate-limiter.limits.group1[1].period: 5M\n"), "application.properties"));

    @Inject
    BucketPodStorage storage;

    @Test
    public void podIsCorrectlyCreatedForAnnotatedMethods() throws NoSuchMethodException {
        BucketPod pod = storage.getBucketPod(RateLimitedMethods.class.getMethod("limited"));
        Assertions.assertNotNull(pod);
        BucketConfiguration configuration = pod.getConfiguration();
        Assertions.assertEquals(2, configuration.getBandwidths().length);
        Assertions.assertEquals(10L, configuration.getBandwidths()[0].getCapacity());
        Assertions.assertEquals(1000_000_000L, configuration.getBandwidths()[0].getRefillPeriodNanos());
        Assertions.assertEquals(100L, configuration.getBandwidths()[1].getCapacity());
        Assertions.assertEquals(300_000_000_000L, configuration.getBandwidths()[1].getRefillPeriodNanos());

    }

    @ApplicationScoped
    public static class RateLimitedMethods {

        @RateLimited(limitsKey = "group1")
        public String limited() {
            return "LIMITED";
        }

    }
}
