package io.quarkiverse.bucket4j.runtime;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.quarkus.runtime.configuration.DurationConverter;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithConverter;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.rate-limiter")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface RateLimiterRuntimeConfig {

    /**
     * rate limiter will be completely disabled if false
     */
    @WithDefault("true")
    boolean enabled();

    /**
     * Maximum number of entries in the underlying cache
     */
    @WithDefault("1000")
    int maxSize();

    /**
     * Duration during which the bucket is kept after last refill if untouched
     */
    @WithDefault("1H")
    @WithConverter(DurationConverter.class)
    Duration keepAfterRefill();

    /**
     * represent a group of limit applied to a method
     * identified by the bucket id
     */
    @ConfigDocMapKey("bucket-id")
    Map<String, Bucket> buckets();

    /**
     * represent one single bucket
     */
    @ConfigGroup
    interface Bucket {

        /**
         * Identity resolver allow to segment the population.
         * Each resolved identity key will have its own quota.
         * this must be a valid CDI bean implementing IdentityResolver.
         */
        @WithDefault("io.quarkiverse.bucket4j.runtime.resolver.ConstantResolver")
        String identityResolver();

        /**
         * limits enforced for this bucket
         */
        List<Limit> limits();

        /**
         * If true, permitted uses are shared for all methods using the same bucket id.
         * If false, each method has its own quota.
         */
        @WithDefault("false")
        Boolean shared();
    }

    /**
     * represent one single limit
     */
    @ConfigGroup
    interface Limit {

        /**
         * Number of usage per period
         */
        int permittedUses();

        /**
         * evaluation period
         */
        @WithConverter(DurationConverter.class)
        Duration period();
    }
}
