# Quarkus Bucket4j
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-1-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

[![Version](https://img.shields.io/maven-central/v/io.quarkiverse.bucket4j/quarkus-bucket4j?logo=apache-maven&style=flat-square)](https://search.maven.org/artifact/io.quarkiverse.bucket4j/quarkus-bucket4j)

Bucket4J is a Java rate-limiting library based on the token-bucket algorithm. Bucket4j is a thread-safe library that can be used in either a standalone JVM application, or a clustered environment. It also supports in-memory or distributed caching via the JCache (JSR107) specification.
This extension allow you to control the request rate sent to your application by using a dead simple API.

## Getting Started

Read the [full documentation](https://quarkiverse.github.io/quarkiverse-docs/quarkus-bucket4j/dev/index.html).


### Usage

#### Throttling a method

Annotate the method that need to be throttled with @RateLimited

``` java
@ApplicationScoped
public static class RateLimitedMethods {

    @RateLimited(bucket = "group1")
    public String limited() {
        return "LIMITED";
    }

}
```

You can also annotate a class, in that case all methods in the class are throttled

``` java
@ApplicationScoped
@RateLimited(bucket = "group1")
public static class RateLimitedMethods {

    public String limited() {
        return "LIMITED";
    }

}
```

And add a limit group using the same limitsKey in the configuration:

``` properties
# burst protection
quarkus.rate-limiter.buckets.group1.limits[0].permitted-uses: 10
quarkus.rate-limiter.buckets.group1.limits[0].period: 1S
# fair use
quarkus.rate-limiter.buckets.group1.limits[1].permitted-uses: 100
quarkus.rate-limiter.buckets.group1.limits[1].period: 5M
```

The bucket can contain multiple limits that will all be enforced.
If multiple methods share the same bucket id, the number of allowed requests is shared for all them.

#### Population Segmentation

If you want to enable throttling per user, simply specify an IdentityKeyResolver in the RateLimited annotation

``` java
@ApplicationScoped
public static class RateLimitedMethods {

    @RateLimited(bucket = "group1", identityResolver = IpResolver.class)
    public String limitedByIp() {
        return "LIMITED";
    }
}
```

IpResolver is provided out of the box. if you want a more complex segmentation, you can implement your own resolver.
A custom resolver must be a valid CDI Bean.
## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="http://loic.pandore2015.fr"><img src="https://avatars.githubusercontent.com/u/10419172?v=4?s=100" width="100px;" alt="Loïc Hermann"/><br /><sub><b>Loïc Hermann</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-bucket4j/commits?author=rmanibus" title="Code">💻</a> <a href="#maintenance-rmanibus" title="Maintenance">🚧</a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!