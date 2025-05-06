package io.quarkiverse.bucket4j.test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.UUID;

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
import io.quarkiverse.bucket4j.runtime.resolver.IdentityResolver;
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
                                    "quarkus.rate-limiter.buckets.shared-method.limits[0].period: 1S\n" +
                                    "quarkus.rate-limiter.buckets.repeated-limit-global.limits[0].permitted-uses: 3\n" +
                                    "quarkus.rate-limiter.buckets.repeated-limit-global.limits[0].period: 10S\n" +
                                    "quarkus.rate-limiter.buckets.repeated-limit-peruser.limits[0].permitted-uses: 1\n" +
                                    "quarkus.rate-limiter.buckets.repeated-limit-peruser.limits[0].period: 1S\n"),
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
                .isBetween(800L, 1000L);
    }

    @Test
    public void rateLimitExceptionIsThrownIfQuotaIsExceededForAtLeastOneMethodAnnotation() {
        // Bucket pods state : global:3, user1:1, user2:1
        methods.limitedByTwoLimits();
        // Bucket pods state : global:2, user1:0, user2:1
        methods.limitedByTwoLimits();
        // Bucket pods state : global:1, user1:0, user2:0
        RateLimitException rateLimitException = Assertions.assertThrows(RateLimitException.class,
                () -> methods.limitedByTwoLimits());
        assertThat(rateLimitException.getWaitTimeInMilliSeconds()).isBetween(800L, 1000L);
    }

    @Test
    public void rateLimitExceptionIsThrownWhenMultipleRateLimitUseTheSameBucketOnAGivenMethod() {
        Assertions.assertThrows(RateLimitException.class, () -> methods.twoLimitsUsingSameBucket());
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
                .isBetween(800L, 1000L);
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

        @RateLimited(bucket = "repeated-limit-global")
        @RateLimited(bucket = "repeated-limit-peruser", identityResolver = FirstCallUser1OtherUser2.class)
        public String limitedByTwoLimits() {
            return "LIMITED";
        }

        @RateLimited(bucket = "annotated-method")
        @RateLimited(bucket = "annotated-method")
        public String twoLimitsUsingSameBucket() {
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

    @ApplicationScoped
    public static class FirstCallUser1OtherUser2 implements IdentityResolver {
        boolean isFirstCall = true;
        String firstCallUUID = UUID.randomUUID().toString();
        String otherCallUUID = UUID.randomUUID().toString();

        @Override
        public String getIdentityKey() {
            if (isFirstCall) {
                isFirstCall = false;
                return firstCallUUID;
            } else {
                return otherCallUUID;
            }
        }
    }
}
