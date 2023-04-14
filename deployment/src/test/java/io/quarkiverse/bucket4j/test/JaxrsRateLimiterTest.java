package io.quarkiverse.bucket4j.test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

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

public class JaxrsRateLimiterTest {

    // Start unit test with your extension loaded
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(RateLimitedMethods.class)
                    .addAsResource(new StringAsset("quarkus.rate-limiter.buckets.group1.limits[0].permitted-uses: 1\n" +
                            "quarkus.rate-limiter.buckets.group1.limits[0].period: 1S"), "application.properties"));

    @Test
    public void status429IsReturnedIfQuotaIsExceeded() {

        given()
                .when()
                .get("/test")
                .then()
                .statusCode(200)
                .body(is("LIMITED"));

        given()
                .when().get("/test")
                .then()
                .statusCode(429)
                .header("Retry-After", is(notNullValue()));

    }

    @RequestScoped
    @Path("/test")
    public static class RateLimitedMethods {

        @GET
        @RateLimited(bucket = "group1", identityResolver = IpResolver.class)
        public String limitedByIp() {
            return "LIMITED";
        }

    }
}
