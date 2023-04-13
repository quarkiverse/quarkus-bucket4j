package io.quarkiverse.bucket4j.test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.bucket4j.runtime.IdentityKeyResolverStorage;
import io.quarkiverse.bucket4j.runtime.RateLimited;
import io.quarkiverse.bucket4j.runtime.resolver.ConstantResolver;
import io.quarkiverse.bucket4j.runtime.resolver.IdentityKeyResolver;
import io.quarkus.test.QuarkusUnitTest;

public class IdentityKeyResolverTest {

    // Start unit test with your extension loaded
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(RateLimitedMethods.class)
                    .addAsResource(new StringAsset("quarkus.rate-limiter.limits.group1[0].max-usage: 10\n" +
                            "quarkus.rate-limiter.limits.group1[0].period: 1S\n"), "application.properties"));

    @Inject
    IdentityKeyResolverStorage storage;

    @Test
    public void podIsCorrectlyCreatedForAnnotatedMethods() throws NoSuchMethodException {
        IdentityKeyResolver resolver = storage.getIdentityKeyResolver(RateLimitedMethods.class.getMethod("limited"));
        assertThat(resolver)
                .isNotNull()
                .isOfAnyClassIn(ConstantResolver.class);
    }

    @ApplicationScoped
    public static class RateLimitedMethods {

        @RateLimited(limitsKey = "group1")
        public String limited() {
            return "LIMITED";
        }

    }
}
