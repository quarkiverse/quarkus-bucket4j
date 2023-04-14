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

@ConfigMapping(prefix = "quarkus.rate-limiter")
@ConfigRoot(phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public interface RateLimiterConfig {

    /**
     * represent a group of limit applied to a method
     * identified by the bucket id
     * If multiple methods share the same bucket id, the permitted uses are shared
     */
    @ConfigDocMapKey("bucket-id")
    Map<String, Bucket> buckets();

    /**
     * represent one single bucket
     */
    @ConfigGroup
    interface Bucket {

        /**
         * limits enforced for this bucket
         */
        List<Limit> limits();

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
