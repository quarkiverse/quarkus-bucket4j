package io.quarkiverse.bucket4j.test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.bucket4j.runtime.RateLimited;
import io.quarkiverse.bucket4j.runtime.resolver.IpResolver;
import io.quarkus.test.QuarkusUnitTest;

public class RateLimitJaxrsTest {

    // Start unit test with your extension loaded
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(RateLimitedMethods.class)
                    .addAsResource(new StringAsset("quarkus.rate-limiter.limits.group1[0].max-usage: 1\n" +
                            "quarkus.rate-limiter.limits.group1[0].period: 1S"), "application.properties"));

    @Test
    public void rateLimitExceptionIsThrownIfQuotaIsExceeded() {

        given()
                .when()
                .get("/test")
                .then()
                .statusCode(200)
                .body(is("LIMITED"));

        given()
                .when().get("/test")
                .then()
                .statusCode(429);

    }

    @RequestScoped
    @Path("/test")
    public static class RateLimitedMethods {

        @GET
        @RateLimited(limitsKey = "group1", identityResolver = IpResolver.class)
        public String limitedByIp() {
            return "LIMITED";
        }

    }
}
