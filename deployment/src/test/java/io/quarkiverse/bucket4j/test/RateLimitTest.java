package io.quarkiverse.bucket4j.test;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.bucket4j.runtime.RateLimitException;
import io.quarkiverse.bucket4j.runtime.RateLimited;
import io.quarkiverse.bucket4j.runtime.resolver.IpResolver;
import io.quarkus.test.QuarkusUnitTest;

public class RateLimitTest {

    // Start unit test with your extension loaded
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(RateLimitedMethods.class)
                    .addAsResource(new StringAsset("quarkus.rate-limiter.limits.group1[0].max-usage: 1\n" +
                            "quarkus.rate-limiter.limits.group1[0].period: 1S"), "application.properties"));

    @Inject
    RateLimitedMethods methods;

    @Test
    public void rateLimitExceptionIsThrownIfQuotaIsExceeded() {
        methods.limited();
        RateLimitException rateLimitException = Assertions.assertThrows(RateLimitException.class, () -> methods.limited());
        assertBetween(900_000_000, 1000_000_000, rateLimitException.getWaitTimeInNanoSeconds());

    }

    private void assertBetween(long min, long max, long value) {
        Assertions.assertTrue(value < max);
        Assertions.assertTrue(value > min);
    }

    @Test
    public void contextNotActiveExceptionIsThrownIfIpResolverIsUsedOutsideRequestContext() {
        Assertions.assertThrows(ContextNotActiveException.class, () -> methods.limitedByIp());
    }

    @ApplicationScoped
    public static class RateLimitedMethods {

        @RateLimited(limitsKey = "group1")
        public String limited() {
            return "LIMITED";
        }

        @RateLimited(limitsKey = "group1", identityResolver = IpResolver.class)
        public String limitedByIp() {
            return "LIMITED";
        }

    }
}
