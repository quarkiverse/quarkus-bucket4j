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
                    .addAsResource(
                            new StringAsset("quarkus.rate-limiter.buckets.annotated-method.limits[0].permitted-uses: 1\n" +
                                    "quarkus.rate-limiter.buckets.annotated-method.limits[0].period: 1S\n" +
                                    "quarkus.rate-limiter.buckets.annotated-class.limits[0].permitted-uses: 1\n" +
                                    "quarkus.rate-limiter.buckets.annotated-class.limits[0].period: 1S\n" +
                                    "quarkus.rate-limiter.buckets.isolated-method.shared: false\n" +
                                    "quarkus.rate-limiter.buckets.isolated-method.limits[0].permitted-uses: 1\n" +
                                    "quarkus.rate-limiter.buckets.isolated-method.limits[0].period: 1S\n" +
                                    "quarkus.rate-limiter.buckets.shared-method.shared: true\n" +
                                    "quarkus.rate-limiter.buckets.shared-method.limits[0].permitted-uses: 1\n" +
                                    "quarkus.rate-limiter.buckets.shared-method.limits[0].period: 1S\n"),
                            "application.properties"));

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
    public void quotaIsIsolatedIfSharingIsDisabled() {
        methods.isolated();
        Assertions.assertDoesNotThrow(() -> methods.isolated("param"));
    }

    @Test
    public void quotaIsSharedIfSharingIsEnabled() {
        methods.shared();
        Assertions.assertThrows(RateLimitException.class, () -> methods.otherShared());
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

        @RateLimited(bucket = "annotated-method")
        public String limited() {
            return "LIMITED";
        }

        @RateLimited(bucket = "isolated-method")
        public String isolated() {
            return "LIMITED";
        }

        @RateLimited(bucket = "isolated-method")
        public String isolated(String param) {
            return "LIMITED";
        }

        @RateLimited(bucket = "shared-method")
        public String shared() {
            return "LIMITED";
        }

        @RateLimited(bucket = "shared-method")
        public String otherShared() {
            return "LIMITED";
        }

        @RateLimited(bucket = "annotated-method", identityResolver = IpResolver.class)
        public String limitedByIp() {
            return "LIMITED";
        }

    }

    @ApplicationScoped
    @RateLimited(bucket = "annotated-class")
    public static class RateLimitedClass {
        public String limited() {
            return "LIMITED";
        }
    }
}
