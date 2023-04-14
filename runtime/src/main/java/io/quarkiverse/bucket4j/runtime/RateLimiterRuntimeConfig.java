package io.quarkiverse.bucket4j.runtime;

import java.time.Duration;

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

}
