package io.quarkiverse.bucket4j.test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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

public class RateLimiterTest {

    // Start unit test with your extension loaded
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(RateLimitedMethods.class)
                    .addClass(RateLimitedClass.class)
                    .addAsResource(new StringAsset("quarkus.rate-limiter.buckets.group1.limits[0].permitted-uses: 1\n" +
                            "quarkus.rate-limiter.buckets.group1.limits[0].period: 1S\n" +
                            "quarkus.rate-limiter.buckets.group2.limits[0].permitted-uses: 1\n" +
                            "quarkus.rate-limiter.buckets.group2.limits[0].period: 1S"), "application.properties"));

    @Inject
    RateLimitedMethods methods;
    @Inject
    RateLimitedClass clazz;

    @Test
    public void rateLimitExceptionIsThrownIfQuotaIsExceededForLevelMethodAnnotation() {
        methods.limited();
        RateLimitException rateLimitException = Assertions.assertThrows(RateLimitException.class, () -> methods.limited());
        assertThat(rateLimitException.getWaitTimeInMilliSeconds())
                .isBetween(800_000L, 1000_000L);
    }

    @Test
    public void rateLimitExceptionIsThrownIfQuotaIsExceededForClassMethodAnnotation() {
        clazz.limited();
        RateLimitException rateLimitException = Assertions.assertThrows(RateLimitException.class, () -> clazz.limited());
        assertThat(rateLimitException.getWaitTimeInMilliSeconds())
                .isBetween(800_000L, 1000_000L);
    }

    @Test
    public void contextNotActiveExceptionIsThrownIfIpResolverIsUsedOutsideRequestContext() {
        Assertions.assertThrows(ContextNotActiveException.class, () -> methods.limitedByIp());
    }

    @ApplicationScoped
    public static class RateLimitedMethods {

        @RateLimited(bucket = "group1")
        public String limited() {
            return "LIMITED";
        }

        @RateLimited(bucket = "group1", identityResolver = IpResolver.class)
        public String limitedByIp() {
            return "LIMITED";
        }

    }

    @ApplicationScoped
    @RateLimited(bucket = "group2")
    public static class RateLimitedClass {
        public String limited() {
            return "LIMITED";
        }
    }
}
