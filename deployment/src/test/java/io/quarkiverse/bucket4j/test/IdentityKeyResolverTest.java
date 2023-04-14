package io.quarkiverse.bucket4j.test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.bucket4j.runtime.IdentityResolverStorage;
import io.quarkiverse.bucket4j.runtime.RateLimited;
import io.quarkiverse.bucket4j.runtime.resolver.ConstantResolver;
import io.quarkiverse.bucket4j.runtime.resolver.IdentityResolver;
import io.quarkus.test.QuarkusUnitTest;

public class IdentityKeyResolverTest {

    // Start unit test with your extension loaded
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(RateLimitedMethods.class)
                    .addAsResource(new StringAsset("quarkus.rate-limiter.buckets.group1[0].permitted-uses: 10\n" +
                            "quarkus.rate-limiter.buckets.group1[0].period: 1S\n"), "application.properties"));

    @Inject
    IdentityResolverStorage storage;

    @Test
    public void identityResolverIsCorrectlyCreatedForAnnotatedMethods() throws NoSuchMethodException {
        IdentityResolver resolver = storage.getIdentityKeyResolver(RateLimitedMethods.class.getMethod("limited"));
        assertThat(resolver)
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
}
