package io.quarkiverse.bucket4j.runtime;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
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
}
