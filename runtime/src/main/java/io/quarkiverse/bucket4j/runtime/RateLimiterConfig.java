package io.quarkiverse.bucket4j.runtime;

import java.time.Duration;
import java.util.List;
import java.util.Map;

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
     * limits
     */
    Map<String, List<Limit>> limits();

    /**
     * represent one single limit
     */
    @ConfigGroup
    interface Limit {
        /**
         * Number of usage per period
         */
        int maxUsage();

        /**
         * evaluation period
         */
        @WithConverter(DurationConverter.class)
        Duration period();
    }
}
