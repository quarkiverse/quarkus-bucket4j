package io.quarkiverse.bucket4j.test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.bucket4j.runtime.RateLimited;
import io.quarkus.test.QuarkusUnitTest;

public class DisabledRateLimiterTest {

    // Start unit test with your extension loaded
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(RateLimitedMethods.class)
                    .addAsResource(new StringAsset(
                            "quarkus.rate-limiter.enabled: false\n" +
                                    "quarkus.rate-limiter.buckets.group1[0].permitted-uses: 1\n" +
                                    "quarkus.rate-limiter.buckets.group1[0].period: 1S"),
                            "application.properties"));

    @Inject
    RateLimitedMethods methods;

    @Test
    public void rateLimitExceptionIsNotThrownIfRateLimiterIsDisabled() {
        methods.limited();
        assertThat(methods.limited()).isEqualTo("LIMITED");
    }

    @ApplicationScoped
    public static class RateLimitedMethods {

        @RateLimited(bucket = "group1")
        public String limited() {
            return "LIMITED";
        }

    }
}
