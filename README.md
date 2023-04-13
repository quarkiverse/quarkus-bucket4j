# Quarkus Bucket4j

[![Version](https://img.shields.io/maven-central/v/io.quarkiverse.bucket4j/quarkus-bucket4j?logo=apache-maven&style=flat-square)](https://search.maven.org/artifact/io.quarkiverse.bucket4j/quarkus-bucket4j)

easy rate-limiting based on token-bucket algorithm

## Getting Started

Read the full documentation [TODO].

### Usage

Annotate the method that need to be throttled with @RateLimited

``` java
@ApplicationScoped
public static class RateLimitedMethods {

    @RateLimited(limitsKey = "group1")
    public String limited() {
        return "LIMITED";
    }

}
```

And add a limit group using the same limitsKey in the configuration:

``` properties
# burst protection
quarkus.rate-limiter.limits.group1[0].max-usage: 10
quarkus.rate-limiter.limits.group1[0].period: 1S
# fair use
quarkus.rate-limiter.limits.group1[1].max-usage: 100
quarkus.rate-limiter.limits.group1[1].period: 5M
```

The limit group can contain multiple limit that will all be enforced.

If you want to enable throttling per user, simply specify an IdentityKeyResolver in the RateLimited annotation

``` java
@ApplicationScoped
public static class RateLimitedMethods {

    @RateLimited(limitsKey = "group1", identityResolver = IpResolver.class)
    public String limitedByIp() {
        return "LIMITED";
    }
}
```

IpResolver is provided out of the box. if you want a more complex segmentation, you can implement your own resolver.
A custom resolver must be a valid CDI Bean.