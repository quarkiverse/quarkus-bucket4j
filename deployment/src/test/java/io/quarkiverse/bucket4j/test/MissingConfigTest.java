package io.quarkiverse.bucket4j.test;

import static org.junit.jupiter.api.Assertions.fail;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.bucket4j.runtime.RateLimited;
import io.quarkus.test.QuarkusUnitTest;

public class MissingConfigTest {

    // Start unit test with your extension loaded
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setExpectedException(IllegalStateException.class)
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(RateLimitedMethods.class));

    @Test
    public void shouldNeverBeReached() {
        fail();
    }

    @ApplicationScoped
    public static class RateLimitedMethods {

        @RateLimited(limitsKey = "group1")
        public String limited() {
            return "LIMITED";
        }

    }
}
