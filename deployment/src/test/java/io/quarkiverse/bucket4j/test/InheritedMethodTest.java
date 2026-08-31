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

/**
 * Reproduces #154: a class-level {@link RateLimited} annotation also intercepts
 * methods inherited from a non-annotated super class. Those inherited methods
 * are not registered as buckets at build time, so invoking one used to fail with
 * a {@link NullPointerException} inside the interceptor. A method with no bucket
 * configured must simply proceed unlimited.
 */
public class InheritedMethodTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(BaseResource.class)
                    .addClass(InheritingResource.class)
                    .addAsResource(new StringAsset(
                            "quarkus.rate-limiter.buckets.annotated-class.limits[0].permitted-uses: 1\n" +
                                    "quarkus.rate-limiter.buckets.annotated-class.limits[0].period: 1S\n"),
                            "application.properties"));

    @Inject
    InheritingResource resource;

    @Test
    public void inheritedMethodIsNotRateLimitedAndDoesNotThrow() {
        // The inherited method has no bucket registered; it must proceed unlimited
        // instead of throwing a NullPointerException from the interceptor.
        assertThat(resource.inherited()).isEqualTo("INHERITED");
        assertThat(resource.inherited()).isEqualTo("INHERITED");
        assertThat(resource.inherited()).isEqualTo("INHERITED");
    }

    public static class BaseResource {
        public String inherited() {
            return "INHERITED";
        }
    }

    @ApplicationScoped
    @RateLimited(bucket = "annotated-class")
    public static class InheritingResource extends BaseResource {
        public String own() {
            return "OWN";
        }
    }
}
