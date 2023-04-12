package io.quarkiverse.bucket4j.runtime;

import java.time.Duration;
import java.util.Map;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "rate-limiter", phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public class RateLimiterConfig {

    /**
     * limits
     */
    public Map<String, Map<String, Limit>> limits;

    /**
     * represent one single limit
     */
    @ConfigGroup
    public static class Limit {
        /**
         * Number of usage per period
         */
        @ConfigItem
        public int maxUsage;

        /**
         * evaluation period
         */
        @ConfigItem
        public Duration period;
    }
}
